package cn.ijingxi.communication;

import java.io.DataInputStream;
import java.io.OutputStream;

/**
 * Created by andrew on 15-12-29.
 */
public interface ITCPServer {
    void Incoming(Object param,DataInputStream in, OutputStream out);
}
