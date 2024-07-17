import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 玩家实体类，因为这个类用于记录信息，所以称为实体类或者pojo类
 * 在网络版中，玩家信息不再单纯是实体，不仅记录信息还有执行逻辑
 * 在服务器，每个Player对象，它本身是一个读线程，负责读取对应的客户端发来的信息并处理
 */
public class Player extends Thread{
    /**
     * 名称
     */
    private String caption;
    /**
     * 口令
     */
    private String password;
    /**
     * 游戏次数
     */
    private int count;
    /**
     * 玩家积分
     */
    private int score;

    /**
     * 玩家状态
     */
    private String online;
    /**
     * 跟客户端进行通信的连接socket
     */
    private Socket socket;
    /**
     * 判断线程是否退出的标志
     */
    private boolean quit;

    /**
     * 向Player对象对应的客户端发送信息
     * @param msg
     */
    public void sendMsg(String  msg){
        if(this.socket==null) return;
        try {
            BufferedWriter writer =new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(msg);
            writer.newLine();
            writer.flush();
        }catch (Exception e){
            System.out.println("向"+caption+"发送信息失败："+msg);
        }
    }

    public void run(){
        /**取得游戏大厅单体对象*/
        GameLoggy loggy = GameLoggy.getInstance();
        BufferedReader reader= null;
        while (!quit){
            try{
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                /**读取客户端发来的信息*/
                String line = reader.readLine();
                System.out.println("服务器接收到："+line);
                String[] s=line.split(";");
                if(s[0].equalsIgnoreCase(MessageType.logout)){//注销时
                    this.socket.close();
                    this.socket=null;
                    this.online="离线";
                    loggy.window.updatePlayer(this);
                    continue;
                }else if (s[0].equalsIgnoreCase(MessageType.CreateDesktop)){
                    String account =s[1];
                    if(loggy.desktop.containsKey(account)){
                        this.sendMsg("createDesktop;error;该用户棋局已经存在");
                        continue;
                    }
                    Desktop d= new Desktop(account);
                    loggy.desktop.put(account,d);
                    this.sendMsg("createDesktop;ok");
                }else if (s[0].equalsIgnoreCase(MessageType.joinDesktop)){
                    String owner=s[1];
                    String joiner=s[2];
                    if(!loggy.desktop.containsKey(owner)){
                        this.sendMsg("joinDesktop;error;该棋局不存在");
                        continue;
                    }
                    Desktop d= loggy.desktop.get(owner);
                    if(d.opponentName==null||d.opponentName.equals("")){
                        d.opponentName=joiner;
                        this.sendMsg("joinDesktop;ok;"+d.toString());
                        this.sendMsg("beginGame;"+owner+";"+joiner);

                        Player ownerPlayer=loggy.players.get(owner);
                        ownerPlayer.sendMsg("joinDesktop;ok;"+d.toString());
                        ownerPlayer.sendMsg("beginGame;"+owner+";"+joiner);
                        continue;
                    }
                    if(!d.byStandNames.contains(joiner)){
                        d.byStandNames.add(joiner);
                    }this.sendMsg("joinDesktop;ok;"+d.toString());
                }else if (s[0].equalsIgnoreCase(MessageType.exitDesktop)){
                    String owner=s[1];
                    String exiter=s[2];
                    if(!loggy.desktop.containsKey(owner)){
                        this.sendMsg("joinDesktop;error;该棋局不存在");
                        continue;
                    }
                    Player ownerPlayer=loggy.players.get(owner);

                    Desktop d= loggy.desktop.get(owner);
                    if(d.creatorName.equals(exiter)){
                        loggy.desktop.remove(owner);
                        this.sendMsg("exitDesktop;ok;"+d.toString());
                    } else if (d.opponentName.equals(exiter)) {
                        d.opponentName="";
                        this.sendMsg("exitDesktop;ok;"+d.toString());
                    } else if (d.byStandNames.contains(exiter)) {
                        d.byStandNames.remove(exiter);
                        this.sendMsg("ExitDesktop;ok;"+d.toString());
                        continue;
                    }
                    ownerPlayer.sendMsg("interruptGame;"+owner+";"+exiter);
                    this.sendMsg("interruptGame"+owner+exiter);
                    for (String bystand:d.byStandNames){
                        Player temp=loggy.players.get(bystand);
                        temp.sendMsg("interruptGame;"+owner+";"+exiter);
                    }
                } else if (s[0].equalsIgnoreCase(MessageType.refreshDesktop)) {
                    StringBuffer str= new StringBuffer();
                    List<Desktop> ds = new ArrayList<Desktop>(loggy.desktop.values());

                    for(int i=0;i<ds.size();i++){
                        String  temp = ds.get(i).toString();
                        temp=temp.replaceAll(";","#");
                        str.append(temp+"$");
                    }
                    String  msg = "refreshDesktop;"+str.toString();
                    this.sendMsg(msg);
                }else if(s[0].equalsIgnoreCase(MessageType.Piece)){
                    String owner=s[1];
                    Desktop d=loggy.desktop.get(owner);
                    Player ownerPlayer =loggy.players.get(owner);
                    ownerPlayer.sendMsg(line);
                    Player opponentPlayer=loggy.players.get(d.opponentName);
                    opponentPlayer.sendMsg(line);

                    for (String byStandName: d.byStandNames){
                        Player temp =loggy.players.get(byStandName);
                        temp.sendMsg(line);
                    }

                }else if(s[0].equalsIgnoreCase(MessageType.InterruptGame)){
                    String owner=s[1];
                    Desktop d=loggy.desktop.get(owner);
                    Player ownerPlayer =loggy.players.get(owner);
                    ownerPlayer.sendMsg(line);
                    Player opponentPlayer=loggy.players.get(d.opponentName);
                    opponentPlayer.sendMsg(line);

                    for (String byStandName: d.byStandNames){
                        Player temp =loggy.players.get(byStandName);
                        temp.sendMsg(line);
                    }
                    d.opponentName="";
                } else if (s[0].equalsIgnoreCase(MessageType.EndGame)) {
                    String owner=s[1];
                    Desktop d=loggy.desktop.get(owner);
                    Player ownerPlayer =loggy.players.get(owner);
                    ownerPlayer.count+=1;
                    ownerPlayer.sendMsg(line);
                    Player opponentPlayer=loggy.players.get(d.opponentName);
                    opponentPlayer.count+=1;
                    opponentPlayer.sendMsg(line);

                    for (String byStandName: d.byStandNames){
                        Player temp =loggy.players.get(byStandName);
                        temp.sendMsg(line);
                    }
                    if(s[2].equals("1")) opponentPlayer.score+=1;
                    else ownerPlayer.score+=1;
                    d.opponentName="";
                    loggy.window.savePlayerFile();
                }else if (s[0].equalsIgnoreCase(MessageType.Talk)){
                    String owner=s[1];
                    Desktop d= loggy.desktop.get(owner);
                    Player ownerPlayer=loggy.players.get(owner);
                    ownerPlayer.sendMsg(line);
                    Player opponentPlayer=loggy.players.get(d.opponentName);
                    opponentPlayer.sendMsg(line);

                    for (String byStandName: d.byStandNames){
                        Player temp =loggy.players.get(byStandName);
                        temp.sendMsg(line);
                    }
                } else if (s[0].equalsIgnoreCase(MessageType.Undo)) {
                    String owner=s[1];
                    Desktop d= loggy.desktop.get(owner);
                    Player ownerPlayer=loggy.players.get(owner);
                    ownerPlayer.sendMsg(line);
                    Player opponentPlayer=loggy.players.get(d.opponentName);
                    opponentPlayer.sendMsg(line);

                    for (String byStandName: d.byStandNames){
                        Player temp =loggy.players.get(byStandName);
                        temp.sendMsg(line);
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
                System.out.println("读取并处理信息出错："+e.getMessage());
                try {
                    Thread.sleep(1000);
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 缺省构造函数
     */
    public Player() {}
    /**
     * 带参数的构造函数
     * @param name
     * @param password
     * @param count
     * @param score
     * @param state
     */
    public Player(String name,String password,int count,int score,String state) {
        this.caption = name;
        this.password = password;
        this.count = count;
        this.score = score;
        this.online = state;
    }
    /**
     * 将当前对象的信息转换为可显示的字符串
     */
    @Override
    public String toString() {
        return caption+","+password+","+count+","+score+","+ online;
    }
    public String getCaption() {
        return caption;
    }
    public void setCaption(String name) {
        this.caption = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public String getOnline() {
        return online;
    }
    public void setOnline(String online) {
        this.online = online;
    }
    public Socket getSocket(){
        return socket;
    }
    public void setSocket(Socket socket){
        this.socket=socket;
    }
}
