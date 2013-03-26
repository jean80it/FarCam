//    Copyright 2013 Giancarlo Todone
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.

package com.jean.farCam;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author Giancarlo Todone
 */
public class ClientThread implements Runnable{

    private static final String TAG = "FarCam";
    
    TakePictureHelper _picHelper = null;
    Socket _clientSocket = null;
    
    final String _sequenceHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head> <title>Remote Camera View</title> <link href=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css\" rel=\"stylesheet\" type=\"text/css\"/> <script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.5/jquery.min.js\"></script> <script src=\"http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js\"></script> <script type=\"text/javascript\" > \nvar frontBuf; \nvar backBuf; \nfunction loadImage(e) { var oldfrontBuf = frontBuf; frontBuf = backBuf; backBuf = oldfrontBuf; frontBuf.unbind(); frontBuf.css(\"zIndex\", 1); loadFeed(); } \nfunction loadFeed() { backBuf.css(\"zIndex\", -1); backBuf.load(loadImage); backBuf.attr(\"src\", \"shot.jpg?r=\" + Math.floor((Math.random()*100000)+1)); } \n$(document).ready(function() { frontBuf = $(\"#img1\"); backBuf = $(\"#img2\"); loadFeed(); }); \n </script></head><body><div><img id=\"img1\" src=\"\" style=\"position:absolute; z-index: 1; \"><img id=\"img2\" src=\"\" style=\"position:absolute; z-index: -1; \"></div></body></html>";
    
    public static ClientThread NewClientThread(Socket clientSocket, TakePictureHelper picHelper)
    {
        ClientThread clientThread = new ClientThread(clientSocket, picHelper);
        Thread th = new Thread(clientThread);
        th.start();
        return clientThread;
    }
    
    public ClientThread(Socket clientSocket, TakePictureHelper picHelper)
    {
        this._picHelper = picHelper;
        this._clientSocket = clientSocket;
    }
    
    public void run() {
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream(), "ISO-8859-2"));
            OutputStream output = _clientSocket.getOutputStream();
            
//            mHandler.post(new Runnable() {
//                @Override public void run() {
//                    Utils.hint(mycontext, "new client connected FROM "+_clientSocket.getInetAddress()+" "+_clientSocket.getPort());
//            }});
            
            String sAll = getStringFromInput(input);
            final String header = sAll.split("\n")[0];
            
            if (header.startsWith("GET /shot.jpg")) {
                byte[] jpegData = _picHelper.GetPicture();
                sendJPEG(output, jpegData);
            } else {
                if (header.startsWith("GET /sequence.htm")) {
                    send(output, _sequenceHtml);
                }
                else
                {
                    send(output, "404 error");	
                }
            }
            
            try {
                input.close();
                output.flush();
                output.close();
                _clientSocket.close();
            } catch (Exception ex) {
                Log.e(TAG, ex+"");
            }
            
        } catch (Exception ex) {
            Log.e(TAG, ""+ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Log.e(TAG, ""+ex);
            }
        }
    }
    
    
    String getStringFromInput(BufferedReader input) {
            StringBuilder sb = new StringBuilder();
            String sTemp; 
            try {
                while (!(sTemp = input.readLine()).equals(""))  {
                    sb.append(sTemp).append("\n");
                }
            } catch (IOException e) {
                return "";
            }

            return sb.toString();
	}
    
    void sendJPEG(OutputStream output, byte[] data)
    {
        try {
            String contentType = "image/JPEG";

            String header=
                "HTTP/1.1 200 OK\n" +
                "Content-type: "+contentType+"\n"+
                "Content-Length: "+data.length+"\n" +
                "\n";

            output.write(header.getBytes());
            output.write(data);

        } catch (Exception ex) {
            Log.e(TAG, ex+"");
        }
    }
    
    void send(OutputStream output, String s) {
	    String header=
                "HTTP/1.1 200 OK\n" +
                "Connection: close\n"+
                "Content-type: text/html; charset=utf-8\n"+
                "Content-Length: "+s.length()+"\n" +
                "\n";

	    try {
	    	output.write((header+s).getBytes());
	    } catch (Exception ex) {
	    	Log.e(TAG, ""+ex);
	    }
	}
	
}
