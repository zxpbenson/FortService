import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


public class FortServiceSocketApi {

    private ServerSocket serverSocket;
    private Executor executor;
    public FortServiceSocketApi(int nThreads, int port){
        executor = Executors.newFixedThreadPool(nThreads);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public FortServiceSocketApi(){
        this(5, 9777);
    }
    
    public void startService(){
        while(true){
            try {
                Socket socket = serverSocket.accept();
                FortServiceApiWorker worker = new FortServiceApiWorker(socket);
                executor.execute(worker);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            } catch (Exception e){
                e.printStackTrace();
                break;
            }
        }
        System.exit(0);
    }
    
    public static void main(String[] args) {
        int nThreads = args.length > 0 ? Integer.valueOf(args[0]) : 5;
        int port = args.length > 1 ? Integer.valueOf(args[1]) : 9777;
        
        FortServiceSocketApi fortServiceSocketApi = null;
        if(args.length > 2){
            fortServiceSocketApi = new FortServiceSocketApi(nThreads, port);
        }else{
            fortServiceSocketApi = new FortServiceSocketApi();
        }
        fortServiceSocketApi.startService();
    }

}

class FortServiceApiWorker implements Runnable{
    private static AtomicLong counter = new AtomicLong(1);
    private long id = counter.getAndIncrement();
    private Socket socket;
    private InputStream is;
    private InputStreamReader isr;
    private BufferedReader br;
    private OutputStream os;
    private OutputStreamWriter osw;
    private BufferedWriter bw;
    
    public FortServiceApiWorker(Socket socket){
        this.socket = socket;
    }
    
    public void run(){
        try {
            openIO();
            work();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeIO();
        }
    }
    
    private void openIO() throws IOException{
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
    
    private String[] prepareArguments(String cmd){
        String[] cmdArr = cmd.split(" ");
        List<String> argList = new ArrayList<String>();
        for(String arg : cmdArr){
            arg = arg.trim();
            if("".equals(arg)){
                
            }else{
                argList.add(arg);
            }
        }
        String[] args = new String[argList.size()];
        int index = 0;
        for(String arg : argList){
            args[index++] = arg;
        }
        return args;
    }
    
//    private String[] subArgs(String[] args, int startIndex){
//        int subArgsLength = args.length - startIndex;
//        String[] subArgs = new String[subArgsLength];
//        for(int index = 0; index < subArgsLength;){
//            subArgs[index++] = args[startIndex++];
//        }
//        return subArgs;
//    }
    
    private void work() throws IOException{
        String cmd = br.readLine();
        System.out.println("FortServiceApiWorker " + id + " get request : " + cmd);
        String[] args = prepareArguments(cmd);
        String response = execute(args);
        System.out.println("FortServiceApiWorker " + id + " send response : " + response);
        response(response);
    }

//    private void response(boolean response) throws IOException{
//        response(""+response);
//    }
    
    private void response(String response) throws IOException{
        bw.write(response + "\n");
        bw.flush();
    }
    
    private String execute(String[] args){
        /*
        if [ "Person" = $1 ]; then
        #echo Person $2 $3 $4
        java -cp /usr/local/fort_append/FortService/catch_pwd.jar:/usr/local/fort_append/FortService/FortService.jar Person $2 $3 $4
       elif [ "Asset" = $1 ]; then
        #echo Asset $2 $3 $4
        java -cp /usr/local/fort_append/FortService/catch_pwd.jar:/usr/local/fort_append/FortService/FortService.jar Asset $2 $3 $4
       elif [ "Authorization" = $1 ]; then
        #echo Asset $2 $3 $4
        java -cp /usr/local/fort_append/FortService/catch_pwd.jar:/usr/local/fort_append/FortService/FortService.jar Authorization $2 $3 $4 $5 $6 $7
       elif [ "Role" = $1 ]; then
        #echo Asset $2 $3 $4
        java -cp /usr/local/fort_append/FortService/catch_pwd.jar:/usr/local/fort_append/FortService/FortService.jar Role $2 $3 $4
       elif [ "Cmd" = $1 ]; then
        #echo Asset $2 $3 $4
        java -cp /usr/local/fort_append/FortService/catch_pwd.jar:/usr/local/fort_append/FortService/FortService.jar Cmd $2
        */
        
        if(args.length < 1)return "";
        
        if("Person".equals(args[0])){
            if(args.length == 3)return ""+Person.authentication(args[1], args[2]);
            if(args.length == 4)return ""+Person.authentication(args[1], args[2], Boolean.parseBoolean(args[3]));
        }
        if("Asset".equals(args[0])){
            if(args.length == 3)return Asset.getAccountPassword(args[1], args[2]);
            if(args.length == 4)return Asset.getAccountPassword(args[1], args[2], Boolean.parseBoolean(args[3]));
        }
        if("Authorization".equals(args[0])){
            if(args.length == 4)return ""+Authorization.authorizeValidate(args[1], args[2], args[3]);
            if(args.length == 5)return ""+Authorization.authorizeValidate(args[1], args[2], args[3], Boolean.parseBoolean(args[4]));
            if(args.length == 6)return Authorization.getAssetCnAndAccountByAuthorizationCn(args[2], args[3]);
            if(args.length == 7)return Authorization.getAssetCnAndAccountByAuthorizationCn(args[2], args[3], Boolean.parseBoolean(args[4]));
        }
        if("Role".equals(args[0])){
            if(args.length == 3)return Role.accountLimited(args[1], args[2]);
            if(args.length == 4)return Role.accountLimited(args[1], args[2], Boolean.parseBoolean(args[3]));
        }
        return "";
    }
    
}