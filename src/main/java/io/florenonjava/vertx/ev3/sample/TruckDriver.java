package io.florenonjava.vertx.ev3.sample;

import ev3dev.actuators.lego.motors.EV3LargeRegulatedMotor;
import ev3dev.actuators.lego.motors.EV3MediumRegulatedMotor;
import ev3dev.sensors.Battery;
import ev3dev.sensors.ev3.EV3IRSensor;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrate How to control a Lego EV3 truck using Vert.x and the Lego EV3
 * Java driver.
 */
public class TruckDriver {

   static final Logger LOGGER = LoggerFactory.getLogger(TruckDriver.class);

   /**
    * Deploy the verticles.
    *
    * @param args list of arguments. Not used.
    */
   public static void main(String[] args) {

      final EV3IRSensor irSensor = new EV3IRSensor(SensorPort.S1);

      final EV3LargeRegulatedMotor leftEngine = new EV3LargeRegulatedMotor(MotorPort.D);
      final EV3LargeRegulatedMotor rightEngine = new EV3LargeRegulatedMotor(MotorPort.C);
      final EV3MediumRegulatedMotor stering = new EV3MediumRegulatedMotor(MotorPort.A);

      Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(1));
      vertx.deployVerticle(new DistanceVerticle(irSensor.getDistanceMode()), res -> {
         if (res.succeeded()) {
            System.out.println(DistanceVerticle.class.getName() + " deployed");
         }
      });
      vertx.deployVerticle(new DirectionVerticle(stering), res -> {
         if (res.succeeded()) {
            LOGGER.debug(DirectionVerticle.class.getName() + " deployed");
         }
      });
      vertx.deployVerticle(new BatteryStatusVerticle(), res -> {
         if (res.succeeded()) {
            System.out.println(BatteryStatusVerticle.class.getName() + " deployed");
         }
      });
      vertx.deployVerticle(new MoveVerticle(leftEngine, rightEngine), res -> {
         if (res.succeeded()) {
            System.out.println(MoveVerticle.class.getName() + " deployed");
         }
      });
   }
}

/**
 * Periodically (100ms) read the distance measured by the IR sensor.
 */
class DistanceVerticle extends AbstractVerticle {

   private final SampleProvider sampleProvider;

   DistanceVerticle(SampleProvider sampleProvider) {
      this.sampleProvider = sampleProvider;
   }

   @Override
   public void init(Vertx vertx, Context context) {
      super.init(vertx, context);
      EventBus eventBus = vertx.eventBus();
      vertx.setPeriodic(100, handler -> {
         float samples[] = new float[sampleProvider.sampleSize()];
         sampleProvider.fetchSample(samples, 0);
         eventBus.publish("ir.distance", (int) samples[0]);
      });
   }
}

/**
 * Read the voltage of the battery. Can be used to stop the truck
 * when the battery is dying.
 */
class BatteryStatusVerticle extends AbstractVerticle {

   private Battery battery = Battery.getInstance();

   @Override
   public void init(Vertx vertx, Context context) {
      super.init(vertx, context);
      vertx.setPeriodic(1000, handler ->
            TruckDriver.LOGGER.info("Battery voltage:" + battery.getVoltageMilliVolt()));
   }
}

/**
 * Verticle to control the movement of the truck.
 * Moves the truck to a distance of 10cm of obstacle
 */
class MoveVerticle extends AbstractVerticle {

   private final EV3LargeRegulatedMotor rightEngine;
   private final EV3LargeRegulatedMotor leftEngine;

   MoveVerticle(EV3LargeRegulatedMotor leftEngine,
                EV3LargeRegulatedMotor rightEngine) {
      super();
      this.leftEngine = leftEngine;
      this.rightEngine = rightEngine;
   }

   @Override
   public void init(Vertx vertx, Context context) {
      super.init(vertx, context);
      EventBus eb = vertx.eventBus();
      leftEngine.resetTachoCount();
      rightEngine.resetTachoCount();

      MessageConsumer<Integer> dc = eb.consumer("ir.distance");
      dc.handler(data -> {
         int distance = data.body();
         if (distance >= 10) {
            leftEngine.setSpeed(500);
            rightEngine.setSpeed(500);

            leftEngine.backward();
            rightEngine.backward();
         } else {
            leftEngine.stop();
            rightEngine.stop();
         }
      });
   }
}

class DirectionVerticle extends AbstractVerticle {

   private final EV3MediumRegulatedMotor steringEngine;

   DirectionVerticle(EV3MediumRegulatedMotor stering) {
      super();
      this.steringEngine = stering;
   }

   @Override
   public void init(Vertx vertx, Context context) {
      super.init(vertx, context);
      vertx.setPeriodic(1000, handler -> {
         steringEngine.rotate(20, true);
         TruckDriver.LOGGER.info("Stuur:" + steringEngine.getPosition());
      });
   }
}

