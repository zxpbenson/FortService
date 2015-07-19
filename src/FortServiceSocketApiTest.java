import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


public class FortServiceSocketApiTest {

    private Executor executor;
    private String ip;
    private int port;
    public FortServiceSocketApiTest(int nThreads, String ip, int port){
        executor = Executors.newFixedThreadPool(nThreads);
        this.ip = ip;
        this.port = port;
    }
    
    public FortServiceSocketApiTest(){
        this(5, "192.168.10.129", 9777);
    }
    
    public void startTest(int limit, String bArg){
        String[] cmds = new String[]{
                "Asset Asset_002A5D869 root " + bArg,
                "Person zhangke 123 " + bArg,
                "Authorization zhangke Asset_1351712111964296 support " + bArg,
                "Authorization get zhangke 13165170732686 - - " + bArg,
                "Role zhangke Asset_0031B20A8 " + bArg,
                "Cmd " + bArg,
                ""
        };
        
        int cmdCounter = 0;
        while(true){
            if(cmdCounter > limit)break;
            FortServiceApiTestWorker worker = new FortServiceApiTestWorker(ip, port, cmds[cmdCounter++ % 7]);
            executor.execute(worker);
        }
        
        
    }
    
    public static void main(String[] args) {
        int nThreads = args.length > 0 ? Integer.valueOf(args[0]) : 10;
        String ip = args.length > 1 ? args[1] : "127.0.0.1";
        int port = args.length > 2 ? Integer.valueOf(args[2]) : 9777;
        
        int limit = args.length > 0 ? Integer.valueOf(args[0]) : 100;
        String bArg = args.length > 1 ? args[1] : "";
        
        FortServiceSocketApiTest fortServiceSocketApiTest = null;
        if(args.length > 2){
            fortServiceSocketApiTest = new FortServiceSocketApiTest(nThreads, ip, port);
        }else{
            fortServiceSocketApiTest = new FortServiceSocketApiTest();
        }
        fortServiceSocketApiTest.startTest(limit, bArg);
    }

}

class FortServiceApiTestWorker implements Runnable{
    private static AtomicLong counter = new AtomicLong(1);
    private long id = counter.getAndIncrement();
    private String ip;
    private int port;
    private String cmd;
    private Socket socket;
    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader br;
    private OutputStream os;
    private OutputStreamWriter osw;
    private BufferedWriter bw;
    private String response;
    
    public FortServiceApiTestWorker(String ip, int port, String cmd){
        this.ip = ip;
        this.port = port;
        this.cmd = cmd;
    }
    
    public void run(){
        try {
            openIO();
            work();
        } catch (IOException e) {
            e.printStackTrace();
        }  catch (Exception e){
            e.printStackTrace();
        } finally {
            closeIO();
        }
    }
    
    private void openIO() throws UnknownHostException, IOException{
        socket = new Socket(ip, port);
        
        is = socket.getInputStream();
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        
        os = socket.getOutputStream();
        osw = new OutputStreamWriter(os);
        bw = new BufferedWriter(osw);
    }
    
    private void closeIO(){
        if(br != null)try{br.close();}catch(IOException e){}
        if(isr != null)try{isr.close();}catch(IOException e){}
        if(is != null)try{is.close();}catch(IOException e){}
        
        if(bw != null)try{bw.close();}catch(IOException e){}
        if(osw != null)try{osw.close();}catch(IOException e){}
        if(os != null)try{os.close();}catch(IOException e){}
            
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void work() throws IOException{
        System.out.println("FortServiceApiTestWorker " + id + " send request : " + cmd);
        request(cmd);
        response = br.readLine();
        System.out.println("FortServiceApiTestWorker " + id + " get response : " + response);
    }

    private void request(String request) throws IOException{
        bw.write(request + "\n");
        bw.flush();
    }

    public String getResponse() {
        return response;
    }
    
}