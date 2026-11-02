package com.example

import com.example.model.CommData
import com.example.model.GpsData
import com.example.model.MissionPhase
import com.example.model.TelemetryPacket
import com.example.service.CsvExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun csvExport_generatesCorrectHeadersAndValues() {
        val packet = TelemetryPacket(
            device_id = "AETHER-CAN-01",
            packet_id = 42,
            timestamp = 1700000000000L,
            iso_timestamp = "2023-11-14T22:13:20.000Z",
            mission_phase = MissionPhase.DESCENT,
            altitude_m = 482.5,
            temperature_c = 25.3,
            pressure_hpa = 960.2,
            humidity_percent = 62.4,
            aqi = 38,
            descent_velocity_ms = -6.2,
            battery_voltage = 8.24,
            gps = GpsData(
                latitude = 13.0827,
                longitude = 80.2707,
                altitude_m = 494.5,
                satellites = 10,
                valid = true
            ),
            communication = CommData(
                rssi = -68,
                snr = 9.5f,
                packet_valid = true,
                crc_ok = true
            ),
            is_simulated = true
        )

        val csv = CsvExporter.generateCsvString(listOf(packet))
        assertTrue(csv.contains("PacketID,Timestamp,ISOTime,MissionPhase"))
        assertTrue(csv.contains("42,1700000000000,\"2023-11-14T22:13:20.000Z\",DESCENT"))
        assertTrue(csv.contains("482.50"))
        assertTrue(csv.contains("-6.20"))
    }
}
