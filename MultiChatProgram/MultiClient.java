import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

//참고 : https://kadosholy.tistory.com/126

public class MultiClient {
    public static void main(String[] args) {
        MultiClient multiClient = new MultiClient();
        multiClient.start();
    }

    public void start() {
        Socket socket = null;
        BufferedReader in = null;

        try {
            socket = new Socket("localhost", 8000);
            System.out.println("서버와 연결되었습니다.");

            String name = "user" + (int)(Math.random() * 10);
            Thread sendTread = new SendTread(socket, name);
            sendTread.start();

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (in != null){
                String inputMsg = in.readLine();
                if((name + "님이 나가셨습니다.").equals(inputMsg)) break;
                System.out.println("From : " + inputMsg);
            }

        } catch (Exception e){
            System.out.println("서버 접속 끊김");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("서버 연결 종료");

    }

}
class SendTread extends Thread {
    Socket socket = null;
    String name;

    Scanner scanner = new Scanner(System.in);

    public SendTread(Socket socket, String name){
        this.socket = socket;
        this.name = name;
    }

    @Override
    public void run() {
        try {
            //최초 1회는 client의 name을 서버에 전송
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.println(name);
            out.flush();

            while (true){
                String outputMsg = scanner.nextLine();
                out.println(outputMsg);
                out.flush();
                if("quit".equals(outputMsg)) break;
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
