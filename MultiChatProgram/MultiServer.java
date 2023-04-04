import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//참고 : https://kadosholy.tistory.com/126

public class MultiServer {
    public static void main(String[] args) {
        MultiServer multiServer = new MultiServer();
        multiServer.start();
    }

    public void start(){
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(8000);
            while (true){
                System.out.println("클라이언트 연결 대기 중");
                socket = serverSocket.accept();

                ReceiveThread receiveThread = new ReceiveThread(socket);
                receiveThread.start();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if(serverSocket != null) {
                try{
                    serverSocket.close();
                    System.out.println("서버 종료");
                } catch (Exception e){
                    e.printStackTrace();
                    System.out.println("서버 소켓 통신 에러");
                }
            }
        }
    }

}
class ReceiveThread extends Thread{
    static List<PrintWriter> list = Collections.synchronizedList(new ArrayList<>());

    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;

    public ReceiveThread (Socket socket){
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            list.add(out);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String name = "";

        try{
            //최초 1회는 client 이름을 수신
            name = in.readLine();
            System.out.println(name + " 새 연결 생성");
            System.out.println(name + " 님이 들어오셨습니다.");

            while (in != null){
                String inputMsg = in.readLine();
                if("quit".equals(inputMsg)) break;
                sendAll(name + " : " + inputMsg);
            }

        } catch (Exception e){
            System.out.println(name + " 접속이 끊겼습니다.");
        } finally {
            sendAll(name + "님이 나가셨습니다.");
            list.remove(out);
            try {
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void sendAll (String s){
        for(PrintWriter out : list){
            out.println(s);
            out.flush();
        }
    }
}
