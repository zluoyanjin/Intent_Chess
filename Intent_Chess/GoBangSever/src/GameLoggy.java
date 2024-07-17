import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏大厅，定义全局变量
 */
public class GameLoggy {
    /**所有玩家信息*/
    public Map<String,Player> players= new HashMap<String,Player>();
    /**所有棋局信息*/
    public Map<String, Desktop> desktop = new HashMap<String,Desktop>();
    /**服务socket类，用于监听端口*/
    public ServerSocket svrSocket;
    /**接收线程*/
    public AcceptThread acceptThread;
    /**玩家信息管理窗口*/
    public MainWindow window;

    /**
     * 将MAP类型转换为List类型
     * @return
     */
    public List<Player>toPlayerList(){
        return new ArrayList<Player>(this.players.values());
    }

    /**
     * 创建监听服务并启动监听线程
     */
    public void beginAcceptThread(){
        try {
            svrSocket =new ServerSocket(9001);
            acceptThread=new AcceptThread();
            acceptThread.start();
        }catch (IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"启动监听端口失败："+e.getMessage());
        }
    }

    /**
     * 单体对象
     * 1）在一个类中定义自己类型的私有的静态的成员变量
     * 2）编写单体方法
     * 3）编写单体对象私有构造函数
     * 以上三步编写的变量和函数都只能拥有一个
     */
    private static GameLoggy instance=null;

    /**
     * 单体方法：判断单体对象是否有效
     * @return
     */
    public static GameLoggy getInstance(){
        if(instance==null) instance = new GameLoggy();
        return instance;
    }

    private GameLoggy(){

    }

    public static void main(String[] args) {
        GameLoggy loggy =GameLoggy.getInstance();
        loggy.window=new MainWindow();
        loggy.window.setVisible(true);

        loggy.beginAcceptThread();
    }


}
