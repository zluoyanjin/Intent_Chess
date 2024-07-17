import java.io.*;
import java.net.Socket;

/**
 * 客户端连接请求
 */
public class AcceptThread extends Thread{
    public boolean quitFlag;
    public AcceptThread(){

    }
    public void run(){
        GameLoggy loggy = GameLoggy.getInstance();
        while (!quitFlag){
            try {
                /** 监听客户端连接*/
                Socket socket = loggy.svrSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                /**等待读取客户发送的Login信息*/
                String loginInfo = reader.readLine();
                System.out.println("服务器端接收到："+loginInfo);
                /**判断账号密码是否正确*/
                String[] infos=loginInfo.split(";");
                String account = infos[1];
                String  password=infos[2];
                /**如果账号不存在*/
                if(!loggy.players.containsKey(account)){
                    writer.write("Lohin;ERROR;账户不存在");
                    writer.newLine();
                    writer.flush();
                    continue;
                }
                /**如果密码不正确*/
                Player player=loggy.players.get(infos[1]);
                if(!player.getPassword().equals(infos[2])){
                    writer.write("Lohin;ERROR;密码不正确");
                    writer.newLine();
                    writer.flush();
                    continue;
                }
                /**修改玩家对象状态*/
                player.setOnline("离线");
                player.setSocket(socket);
                if(!player.isAlive()){
                    player.start();
                }

                /**修改界面的玩家状态*/
                loggy.window.updatePlayer(player);

                writer.write("Login;ok");
                writer.newLine();
                writer.flush();
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("接受客服端连接出错："+e.getMessage());
            }
        }
    }
}
