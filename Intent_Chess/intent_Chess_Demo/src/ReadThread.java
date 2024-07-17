import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 客户端的网络读线程
 */
public class ReadThread extends Thread{
    /**游戏窗口的引用*/
    private Chessboard chessboard;
    /**读取信息的通信连接*/
    private Socket socket;
    /**读线程的结束标志*/
    public boolean quit = false;

    /**
     * 构造函数
     * @param chessboard
     * @param socket
     */
    public ReadThread(Chessboard chessboard,Socket socket){
        this.chessboard=chessboard;
        this.socket=socket;
    }

    /**
     * 线程运行函数
     */
    public void run(){
        while(!quit){
            try {
                /**读取网络信息*/
                BufferedReader reader =new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String resp= reader.readLine();
                System.out.println(chessboard.loginFrame.getAccount()+"客户端收到："+resp);
                String[] s=resp.split(";");
                //根据取得服务器端发来的信息的第一项判断做说明处理
                if(s[0].equalsIgnoreCase(MassgateType.refreshDesktop)){
                    doRefreshDesktop(resp,s);
                } else if (s[0].equalsIgnoreCase(MassgateType.CreateDesktop)) {
                    doCreateDesktop(resp,s);
                } else if (s[0].equalsIgnoreCase(MassgateType.joinDesktop)) {
                    doJoinDesktop(resp,s);
                } else if (s[0].equalsIgnoreCase(MassgateType.exitDesktop)) {
                    doExitDesktop(resp,s);
                } else if (s[0].equalsIgnoreCase(MassgateType.BeginGame)) {
                    doBeginGame(resp,s);
                } else if (s[0].equalsIgnoreCase(MassgateType.InterruptGame)) {
                    doInterruptGame(resp,s);
                } else if (s[0].equalsIgnoreCase(MassgateType.EndGame)) {
                    doEndGame(resp,s);
                } else if (s[0].equalsIgnoreCase(MassgateType.Piece)) {
                    doPiece(resp,s);
                } else if (s[0].equalsIgnoreCase(MassgateType.Talk)) {
                    chessboard.receiveTalk(s[2],s[3]);
                }else if(s[0].equalsIgnoreCase(MassgateType.Undo)){
                    doUndo(resp,s);
                }
            }catch (Exception e3){
                e3.printStackTrace();
                System.out.println("读取信息发生错误："+e3.getMessage());
                return;
            }
        }
    }

    /**
     * 处理落子信息
     * @param resp
     * @param s
     */
    private void doPiece(String resp,String[] s){
        int color=Integer.parseInt(s[3]);
        int row=Integer.parseInt(s[4]);
        int col=Integer.parseInt(s[5]);
        chessboard.putPiece(color,row,col);
    }

    private void doUndo(String resp,String[] s){
        int color= Integer.parseInt(s[3]);
        int row=Integer.parseInt(s[4]);
        int col=Integer.parseInt(s[5]);
        chessboard.Undo(color,row,col);
    }


    /**
     * 结束游戏
     * @param resp
     * @param s
     */
    private void doEndGame(String resp,String[] s){
        chessboard.init();
    }

    /**
     * 终止游戏
     * @param resp
     * @param s
     */
    private void doInterruptGame(String resp,String[] s){
        String creator=s[1];
        String opponent=s[2];
        chessboard.stop();
    }

    /**
     * 开始游戏
     * @param resp
     * @param s
     */
    private void doBeginGame(String resp,String[] s){
        String creator=s[1];
        String opponent=s[2];

        String myName=chessboard.loginFrame.getAccount();
        if(myName.equals(creator))chessboard.setPlayer(opponent,myName);
        else if(myName.equals(opponent))chessboard.setPlayer(myName,creator);
        else chessboard.setPlayer(opponent,creator);
        chessboard.start();
    }

    /**
     * 退出桌面
     * @param resp
     * @param s
     */
    private void doExitDesktop(String resp,String[] s){
        if(!s[1].equalsIgnoreCase("ok")){
            JOptionPane.showMessageDialog(null,"退出失败，服务器端返回："+s[2]);
            return;
        }
        String createName=s[2];
        String opponentName=s.length>=4?s[3]:"";
        String observerList =s.length>=5?s[4]:"";
        chessboard.loginFrame.updateDesktop(createName,opponentName,observerList);
        chessboard.stop();
    }

    /**
     * 加入棋局
     * @param resp
     * @param s
     */
    private void doJoinDesktop(String resp,String[] s){
        if(!s[1].equalsIgnoreCase("ok")){
            JOptionPane.showMessageDialog(null,"加入失败，服务器端返回："+s[2]);
            return;
        }
        JOptionPane.showMessageDialog(null,"加入成功");
        String createName=s[2];
        String opponentName=s.length>=4?s[3]:"";
        String observerList =s.length>=5?s[4]:"";

        //修改表格
        chessboard.loginFrame.updateDesktop(createName,opponentName,observerList);
        //窗口隐藏掉
        chessboard.loginFrame.setVisible(false);


        //初始化棋局
        chessboard.setPlayer(opponentName,createName);
    }

    /**
     * 创建棋局
     * @param resp
     * @param s
     */
    private void doCreateDesktop(String resp,String[] s){
        if(!s[1].equalsIgnoreCase("ok")){
            JOptionPane.showMessageDialog(null,"创建失败，服务器端返回："+s[2]);
            return;
        } else JOptionPane.showMessageDialog(null,"创建成功");
        //创建新棋局信息
        chessboard.loginFrame.updateDesktop(chessboard.loginFrame.getAccount(),"","");
    }

    /**
     * 获取所有棋局信息
     * @param resp
     * @param s
     */
    private void doRefreshDesktop(String resp,String[] s){
        //取得棋局表格的数据默写对象
        DefaultTableModel dtm =(DefaultTableModel) chessboard.loginFrame.tableDesktops.getModel();
        dtm.setRowCount(0);

        if(s.length<=1) return;

        String[] desktops=s[1].split("\\$");
        if (desktops==null || desktops.length<=0) return;

        for (int i=0;i<desktops.length;i++){
            if(desktops[i]==null||desktops[i].trim().equals("")) return;
            String[] ss=desktops[i].split("#");
            if(ss==null||ss.length<=0) continue;
            if(ss[0]==null||ss[0].equals("")) continue;
            for (int ii=0;ii<ss.length;ii++){
                System.out.println(ss[ii]);
            }
            dtm.addRow(new Object[]{
               ss[0],
               ss.length>=2?ss[1]:"",
               ss.length>=3?ss[2]:"",
            });
        }
        //刷新表格
        chessboard.loginFrame.tableDesktops.invalidate();
    }

}
