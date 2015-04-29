import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class Cmd{

    public static void main(String[] args){
        Cmd cmd = new Cmd();
        
        //一个命令 配合一个exit能让进程执行完上一个命令后立即结束，不用等待Time超时。
        List[] echoListArr = cmd.execCmdsInProcess(new String[]{"/bin/sh"}, null, null, new String[]{"FortService Role zhangke Asset_0031B20A8 " + (args.length > 0 ? args[0] : ""), "exit"}, 1);
        //List[] echoListArr = cmd.execCmdsInProcess(new String[]{"cmd"}, null, null, new String[]{"ping baidu.com", "exit"}, 1);
        System.out.println("echo begin : ---------------------");
        for(Object echo : echoListArr[0]){
            System.out.println(echo);
        }
        System.out.println("err_echo begin : ---------------------");
        for(Object echo : echoListArr[1]){
            System.out.println(echo);
        }
    }
    
    /*
     * 返回结果为一个长度为2的数组 List[0]是常规回显 List[1]是错误回显
     * programAndArguments:在windows上执行为cmd linux上为 /bin/sh
     * ev:环境变量键值对
     * dir:执行目录
     * String:命令数组
     * timeout:超时时间 单位为分钟
     */
    public List[] execCmdsInProcess(String[] programAndArguments,Map<String,String> ev, String dir, String[] cmds,int timeout){
        Process proc = null;
        OutputStream os = null;
        InputStream is = null;
        InputStream eis = null;
        List<String> echoList = new ArrayList<String>();
        List<String> errEchoList = new ArrayList<String>();
        
        try {
            ///*
            ProcessBuilder pb = new ProcessBuilder(programAndArguments);
            //ProcessBuilder pb = new ProcessBuilder("cmd");
            //ProcessBuilder pb = new ProcessBuilder("/bin/sh");
            Map<String,String> env = pb.environment();
            if(ev != null){
                env.putAll(ev);
            }
            if(dir != null){
                pb.directory(new File(dir));
            }
            System.out.println("Cmd process start.");
            proc = pb.start();
            
            Timer timer = new Timer();
            timer.schedule(new ProcessTimeoutProctor(proc,errEchoList), 1000*60*timeout);
            
            //proc = Runtime.getRuntime().exec("/bin/sh");
            //proc = Runtime.getRuntime().exec("cmd");
            os = proc.getOutputStream();
            is = proc.getInputStream();
            eis = proc.getErrorStream();
            
            EchoProcessThread echoThread = new EchoProcessThread(is,echoList,"Echo");
            echoThread.start();
            EchoProcessThread errEchoThread = new EchoProcessThread(eis,errEchoList,"Err Echo");
            errEchoThread.start();

            for(String cmd : cmds){
                System.out.println("Cmd input:"+cmd);
                os.write((cmd + "\n").getBytes());
                os.flush();
            }
            System.out.println("cmd execute begin wait------");
            proc.waitFor();
            System.out.println("cmd execute end------");
            timer.cancel();//退出进程保护任务
            proc.destroy();
            System.out.println("Cmd process end.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occured in cmd executing:" + e.getMessage());
            echoList.add("Error occured in cmd executing:" + e.getMessage());
        } finally{
            try{
                if(os != null){
                    os.close();
                }
            }catch(Exception e){e.printStackTrace();}
            try{
                if(is != null){
                    is.close();
                }
            }catch(Exception e){e.printStackTrace();}
            try{
                if(eis != null){
                    eis.close();
                }
            }catch(Exception e){e.printStackTrace();}
            try{
                if(proc != null){
                    proc.destroy();
                }
            }catch(Exception e){e.printStackTrace();}
        }
        
        List[] result = new List[]{echoList, errEchoList};
        
        return result;
    }
    
}

class ProcessTimeoutProctor extends TimerTask{
    private Process proc;
    private List<String> errEchoList;
    ProcessTimeoutProctor(Process proc,List<String> errEcho){
        this.proc = proc;
        this.errEchoList = errEcho;
    }
    public void run(){
        errEchoList.add("Process is terminated automaticlly for timeout");
        proc.destroy();
    }
}

class EchoProcessThread extends Thread{
    private InputStream is = null;
    private List<String> echoList = null;
    private String info = null;
    
    public EchoProcessThread(InputStream is, List<String> echoList,String info){
        this.is = is;
        this.echoList = echoList;
        this.info = info;
    }
    
    public void run(){
        System.out.println("Sub thread("+this.info+")begin.");
        InputStreamReader isr = new InputStreamReader(this.is);
        BufferedReader br = new BufferedReader(isr);
        String oneLine = null;
        try{
            while((oneLine = br.readLine()) != null){
                System.out.println(this.info+" info-->"+oneLine);
                this.echoList.add(oneLine);
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Error occured while reading("+this.info+")->"+e.getMessage());
            this.echoList.add("Error occured while reading("+this.info+")->"+e.getMessage());
        }finally{
            try{
                br.close();
            }catch(Exception e){e.printStackTrace();}
            try{
                isr.close();
            }catch(Exception e){e.printStackTrace();}
        }
        System.out.println("Sub thread("+this.info+")end.");
    }
}


