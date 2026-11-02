package com.example.service

import android.content.Context
import android.content.Intent
import com.example.model.TelemetryPacket
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun generateCsvString(packets: List<TelemetryPacket>): String {
        val sb = StringBuilder()
        // Header
        sb.append("PacketID,Timestamp,ISOTime,MissionPhase,Altitude_m,Temperature_C,Pressure_hPa,Humidity_pct,AQI,DescentVelocity_ms,BatteryVoltage_V,GPS_Lat,GPS_Lon,GPS_Alt_m,GPS_Sats,GPS_Valid,LoRa_RSSI_dBm,LoRa_SNR_dB,PacketValid,IsSimulated\n")

        for (p in packets) {
            sb.append(p.packet_id).append(",")
            sb.append(p.timestamp).append(",")
            sb.append("\"").append(p.iso_timestamp).append("\",")
            sb.append(p.mission_phase.name).append(",")
            sb.append(p.altitude_m?.let { String.format(Locale.US, "%.2f", it) } ?: "").append(",")
            sb.append(p.temperature_c?.let { String.format(Locale.US, "%.2f", it) } ?: "").append(",")
            sb.append(p.pressure_hpa?.let { String.format(Locale.US, "%.2f", it) } ?: "").append(",")
            sb.append(p.humidity_percent?.let { String.format(Locale.US, "%.1f", it) } ?: "").append(",")
            sb.append(p.aqi?.toString() ?: "").append(",")
            sb.append(p.descent_velocity_ms?.let { String.format(Locale.US, "%.2f", it) } ?: "").append(",")
            sb.append(p.battery_voltage?.let { String.format(Locale.US, "%.3f", it) } ?: "").append(",")
            sb.append(p.gps.latitude?.let { String.format(Locale.US, "%.6f", it) } ?: "").append(",")
            sb.append(p.gps.longitude?.let { String.format(Locale.US, "%.6f", it) } ?: "").append(",")
            sb.append(p.gps.altitude_m?.let { String.format(Locale.US, "%.1f", it) } ?: "").append(",")
            sb.append(p.gps.satellites).append(",")
            sb.append(p.gps.valid).append(",")
            sb.append(p.communication.rssi ?: "").append(",")
            sb.append(p.communication.snr?.let { String.format(Locale.US, "%.1f", it) } ?: "").append(",")
            sb.append(p.communication.packet_valid).append(",")
            sb.append(p.is_simulated)
            sb.append("\n")
        }
        return sb.toString()
    }

    fun exportAndShareCsv(context: Context, packets: List<TelemetryPacket>) {
        if (packets.isEmpty()) return
        val csvContent = generateCsvString(packets)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "AETHERVIEW_ADVAITA_TELEMETRY_$timeStamp.csv"

        try {
            val file = File(context.cacheDir, fileName)
            val writer = FileWriter(file)
            writer.write(csvContent)
            writer.close()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "AetherView Advaita Telemetry Export ($fileName)")
                putExtra(Intent.EXTRA_TEXT, csvContent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Telemetry CSV"))
        } catch (e: Exception) {
            // Fallback plain share
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "AetherView Advaita Telemetry Log")
                putExtra(Intent.EXTRA_TEXT, csvContent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export Telemetry CSV"))
        }
    }
}
