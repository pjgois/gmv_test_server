package com.gmv

import java.io.*
import java.net.ServerSocket
import java.text.MessageFormat
import kotlin.concurrent.thread

const val SERVER_PORT = 9998
const val STX: Byte = 0x02
const val RS: Byte = 0x1E
const val ETX: Byte = 0x03

class ClientHandler {

    fun start() {

        val serverSocket = ServerSocket(SERVER_PORT)
        println("Server is running on port ${serverSocket.localPort}")
        serverSocket.use {
            while (!serverSocket.isClosed) {
                val client = serverSocket.accept()
                println("Client connected with IP address ${client.inetAddress.hostAddress}")
                thread(start = true) {
                    val dataInputStream = DataInputStream(BufferedInputStream(client.getInputStream()))
                    val dataOutputStream = DataOutputStream(client.getOutputStream())
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    while (!client.isClosed) {
                        try {
                            when (val byteRead = dataInputStream.readByte()) {
                                STX -> byteArrayOutputStream.write(byteRead.toInt())
                                ETX -> {
                                    byteArrayOutputStream.write(byteRead.toInt())
                                    println(MessageFormat.format("Received new message: {0}", byteArrayOutputStream))
                                    dataOutputStream.write(toBytes("OK"))
                                    byteArrayOutputStream.reset()
                                }
                                else -> byteArrayOutputStream.write(byteRead.toInt())
                            }
                        } catch (e: IOException) {
                            client.close()
                            println("${client?.inetAddress?.hostAddress} closed the connection")
                        }
                    }
                }
            }
        }
    }

    private fun toBytes(message: String): ByteArray {

        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                byteArrayOutputStream.write(STX.toInt())
                byteArrayOutputStream.write(message.toByteArray())
                byteArrayOutputStream.write(ETX.toInt())
                return byteArrayOutputStream.toByteArray()
            }
        } catch (e: IOException) {
            println(MessageFormat.format("Error while processing message: {0}", e.message))
            return ByteArray(0)
        }
    }
}