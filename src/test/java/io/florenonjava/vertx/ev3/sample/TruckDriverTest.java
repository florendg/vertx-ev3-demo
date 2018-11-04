package io.florenonjava.vertx.ev3.sample;


import ev3dev.hardware.EV3DevFileSystem;
import ev3dev.hardware.EV3DevPlatform;
import ev3dev.hardware.EV3DevPlatforms;
import ev3dev.sensors.ev3.EV3IRSensor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.SensorMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TruckDriverTest {


   @DisplayName("Demo")
   @Test
   void demonstrateFakeEv3() {
      assertEquals(System.getProperty("user.dir") + "/target/ev3", EV3DevFileSystem.getRootPath(), "Tests should run build directory");
   }

   @Test
   void initPlatformTest() {
      EV3DevPlatform platform = EV3DevPlatforms.getPlatform();
      assertEquals(platform, EV3DevPlatform.EV3BRICK);
   }

   @Disabled
   @Test
   void initInfraRedSensor() {
      EV3IRSensor sensor = new EV3IRSensor(SensorPort.S1);
      SensorMode mode = sensor.getDistanceMode();
      assertNotNull(mode);
   }


}