import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class LoginFrame extends JFrame implements ActionListener {
   /**输入IP的文本框*/
   private JTextField textServerIp;
   /**输入端口号的文本框*/
   private JTextField textServerPort;
   /**输入账号的文本框*/
   private JTextField textAccount;
   /**输入密码的文本框*/
   private JPasswordField passwordField;
   /**登录按钮*/
   private JButton btnLogin;
   /**注销按钮*/
   private JButton btnLogout;
   /**显示棋桌局面的表格*/
   public JTable tableDesktops;
   /**创建棋桌的按钮*/
   private JButton btnDesktopCreate;
   /**加入棋桌的按钮*/
   private JButton btnDesktopJoin;
   /**退出棋桌的按钮*/
   private JButton btnDesktopExit;
   /**重新显示所有已经建立好的的棋桌按钮*/
   private JButton btnDesktopRefresh;

   /**保留游戏界面的引用*/
   private Chessboard chessboard;
   /**客户端到服务器的通信连接*/
   private Socket socket;
   /**一个单独的读线程，用于反复读取从服务器发送来的消息*/
   private ReadThread readThread;

   /**
    * 构造函数
    * @param chessboard
    */
   public LoginFrame(Chessboard chessboard){

       /**记录游戏窗口的地址*/
       this.chessboard=chessboard;
       /**将自己的布局设置为null，里面的所有组件都设置为固定坐标*/
       Container pane =this.getContentPane();
       pane.setLayout(null);

       /**显示服务器IP和IP的输入数*/
       JLabel lblNewLabel =new JLabel("服务器IP：");
       lblNewLabel.setBounds(10,26,67,15);
       pane.add(lblNewLabel);

       textServerIp=new JTextField();
       textServerIp.setBounds(71,23,195,21);
       pane.add(textServerIp);
       textServerIp.setColumns(10);

       /**显示服务器端口和端口的输入框*/
       JLabel lblNewLabel_1 =new JLabel("端口：");
       lblNewLabel_1.setBounds(276,26,55,15);
       pane.add(lblNewLabel_1);

       textServerPort=new JTextField();
       textServerPort.setColumns(10);
       textServerPort.setBounds(329,23,69,21);
       pane.add(textServerPort);

       /**显示账号和账号的输入框*/
       JLabel lblNewLabel_2 =new JLabel("账号：");
       lblNewLabel_2.setBounds(23,68,46,15);
       pane.add(lblNewLabel_2);

       textAccount=new JTextField();
       textAccount.setColumns(10);
       textAccount.setBounds(71,65,195,21);
       pane.add(textAccount);

       /**显示密码和密码的输入框*/
       JLabel lblNewLabel_3 =new JLabel("密码：");
       lblNewLabel_3.setBounds(276,68,46,15);
       pane.add(lblNewLabel_3);

       passwordField=new JPasswordField();
       passwordField.setBounds(329,65,195,21);
       pane.add(passwordField);

       /**显示登录按钮*/
       btnLogin=new JButton("登录");
       btnLogin.setBounds(117,117,93,23);
       btnLogin.addActionListener(this);
       pane.add(btnLogin);

       /**显示登录按钮*/
       btnLogout=new JButton("注销");
       btnLogout.setBounds(330,117,93,23);
       btnLogout.addActionListener(this);
       pane.add(btnLogout);

       /**显示棋桌表格*/
       String[] columns= new String[]{"创建者","对弈者","旁观者"};
       DefaultTableModel model = new DefaultTableModel(null,columns);//存行和列
       tableDesktops = new JTable(model);
       JScrollPane jp = new JScrollPane(tableDesktops);
       jp.setBounds(10,150,524,297);
       pane.add(jp);

       /**显示创建棋桌按钮*/
       btnDesktopCreate = new JButton("创建棋桌");
       btnDesktopCreate.setBounds(10,457,106,23);
       btnDesktopCreate.addActionListener(this);
       pane.add(btnDesktopCreate);

       /**显示加入棋桌表格*/
       btnDesktopJoin =new JButton("加入棋桌");
       btnDesktopJoin.setBounds(153,457,100,23);
       btnDesktopJoin.addActionListener(this);
       pane.add(btnDesktopJoin);

       /**显示退出桌面按钮*/
       btnDesktopExit =new JButton("退出桌面");
       btnDesktopExit.setBounds(300,457,98,23);
       btnDesktopExit.addActionListener(this);
       pane.add(btnDesktopExit);

       /**显示刷新列表按钮*/
       btnDesktopRefresh = new JButton("刷新列表");
       btnDesktopRefresh.setBounds(441,457,93,23);
       btnDesktopRefresh.addActionListener(this);
       pane.add(btnDesktopRefresh);

       /**设置登录窗口的大小*/
       this.setSize(560,529);
       this.setLocationRelativeTo(null);
       this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);//隐藏窗口，为让登录窗口多次显示

    }

    /**
     * 更新桌面，当表格中的某一个棋桌信息发生变化，例如有别的客户端加入或者退出
     * 或者新建一个棋桌的时候网络服务器将棋桌变化信息发送过来的时候
     * 需要更新相应的棋桌表格，调用该函数
     * @param creator  棋桌创建者名称
     * @param opponent 棋桌的对弈者名称
     * @param observers 棋桌的观察者名称
     */
    public  void updateDesktop(String creator,String opponent,String observers){
        /**取得表格数据默写对象*/
        DefaultTableModel dtm = (DefaultTableModel)tableDesktops.getModel();
        /**取得表格当前总行数*/
        int count = dtm.getRowCount();
        /**查找表格中对应的棋桌所对应的index*/
        int index = -1;
        for(int i=0;i<count;i++){
            String name =(String) dtm.getValueAt(i,0);
            if(name.equals(creator)){
                index=i;break;
            }
        }
        if(index<0){
            dtm.addRow(new Object[]{creator,opponent,observers});
        }
        else {
            dtm.setValueAt(creator,index,0);
            dtm.setValueAt(opponent,index,1);
            dtm.setValueAt(observers,index,2);
        }
        /**刷新表格*/
        tableDesktops.invalidate();
    }

    /**
     * 取得当前用户名称
     * @return
     */
    public String getAccount(){
        return this.textAccount.getText();
    }

    /**
     * 点击按钮的处理函数
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
          if(e.getSource()==this.btnLogin)//点击登录按钮
              doLogin();
          else if(e.getSource()==this.btnLogout)//点击退出按钮
              doLogout();
          else if(e.getSource()==this.btnDesktopCreate)//点击创建棋盘按钮
              doDesktopCreate();
          else if (e.getSource()==this.btnDesktopJoin)
              doDesktopJoin();
          else if(e.getSource()==this.btnDesktopExit)//点击退出棋盘按钮
              doDesktopExit();
          else if(e.getSource()==this.btnDesktopRefresh)//点击刷新表格按钮
              doDesktopRefresh();
    }

    /**
     * 点击登录按钮的处理函数
     */
    private void doLogin(){
        /**1 取得服务器ip*/
        String ip =this.textServerIp.getText();
        if(ip.equals("")){
            JOptionPane.showMessageDialog(this,"必须填写服务器IP");
            return;
        }
        /**2 取得服务器端口*/
        int port =Integer.parseInt(this.textServerPort.getText());
        /**3 执行连接*/
        try{
            if(socket==null){
                socket=new Socket(ip,port);
            }
        }catch (Exception e1){
            JOptionPane.showMessageDialog(this,"连接失败"+e1.getMessage());
            return;
        }
        /**4 取得网络输出流，将账户密码发过去*/
        try{
            String account = this.textAccount.getText();
            String passwprd =new String(this.passwordField.getPassword());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write("Lohin;"+account+";"+passwprd);
            writer.newLine();
            writer.flush();
            System.out.println(this.getAccount()+"客户端发送:"+"Login;"+account+";"+passwprd);
        }catch (Exception e2){
            JOptionPane.showMessageDialog(this,"连接成功，但是发送账户密码失败；"+e2.getMessage());
            return;
        }
        /**5 取得网络输入流，等到接受服务端返回登录是否成功信息*/
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String resp =reader.readLine();
            System.out.println(this.getAccount()+"客户端接收:"+resp);
            String[] s=resp.split(";");
            if(s[1].equalsIgnoreCase("ok"))
                JOptionPane.showMessageDialog(this,"登录成功");
            else{
                JOptionPane.showMessageDialog(this,"登录失败,服务器端返回:"+s[2]);
                return;
            }
        }catch (Exception e3){
            JOptionPane.showMessageDialog(this,"账户已发送，但是等待接收返回时发生错误:"+e3.getMessage());
            return;
        }
        //程序登录成功
        chessboard.setTitle("五子棋-"+this.getAccount());
        /**6 启动一个读线程，用于反复读取服务器端发来的信息*/
        if(readThread==null){
            readThread=new ReadThread(chessboard,socket);
            readThread.start();
        }
    }

    /**
     * 点击注销按钮的处理函数
     */
    private void  doLogout(){
        //如果网络通信还没建立
        if(socket==null) return;
        try{
            //发送字符串”Logout;账号名“到服务器
            String account = this.textAccount.getText();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write("Logout;"+account);
            writer.newLine();
            writer.flush();
            System.out.println(this.getAccount()+"客户端发送："+"Logout"+account);
            JOptionPane.showMessageDialog(this,"登录已退出");
            socket.close();
            readThread.quit=true;
            readThread.interrupt();//读线程可能阻塞,所以必须要加
        }catch (Exception e){
            e.printStackTrace();
        }finally {//使登录函数能多次执行
            socket = null;
            readThread = null;
        }
    }

    /**
     * 点击创建棋盘的处理函数
     */
    private void doDesktopCreate(){
        //1 生成要发送的字符串
        String myName = this.textAccount.getText();
        String str = "CreateDesktop;"+myName;

        //2 向服务器发送创建棋盘请求
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(str);
            writer.newLine();
            writer.flush();
            System.out.println(this.getAccount()+"客户端发送："+str);
        }catch (Exception e2){
            JOptionPane.showMessageDialog(this,"发送创建棋盘请求失败："+e2.getMessage());
            return;
        }
        //服务器会返回创建棋盘是否成功的信息，但是不在这里处理
        //因为所有服务器返回的信息将统一在ReadThread处理

    }

    /**
     * 加入棋局按钮的处理函数
     */
    private void doDesktopJoin(){
        //1 取得自己的名字
        String myName = this.textAccount.getText();
        //2 取得该行的创建者名称，对弈者名称
        DefaultTableModel tableModel = (DefaultTableModel) tableDesktops.getModel();
        int seletedRowIndex = tableDesktops.getSelectedRow();
        if(seletedRowIndex<0) return;
        String createor_name = (String) tableModel.getValueAt(seletedRowIndex,0);
        String component_name = (String) tableModel.getValueAt(seletedRowIndex,1);
        //3 不能加入自己创建的棋局
        if(createor_name.equals(myName)){
            JOptionPane.showMessageDialog(this,"不能加入到自己创建的棋局");
            return;
        }

        //4 向服务器发送加入请求
        String str = "JoinDesktop;"+createor_name+";"+myName;
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(str);
            writer.newLine();
            writer.flush();
            System.out.println(this.getAccount()+"客户端发送："+str);
        }catch (Exception e2){
            JOptionPane.showMessageDialog(this,"发送加入棋盘请求失败："+e2.getMessage());
            return;
        }
    }

    /**退出桌面按钮处理函数*/
    private void doDesktopExit(){
        //1 取得自己的名字
        String myName = this.textAccount.getText();
        //2 取得该行的创建者名称，对弈者名称
        DefaultTableModel tableModel = (DefaultTableModel) tableDesktops.getModel();
        int seletedRowIndex = tableDesktops.getSelectedRow();
        if(seletedRowIndex<0) return;
        String createor_name = (String) tableModel.getValueAt(seletedRowIndex,0);
        String component_name = (String) tableModel.getValueAt(seletedRowIndex,1);

        //3 向服务器发送退出请求
        String str = "ExitDesktop;"+createor_name+";"+myName;
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(str);
            writer.newLine();
            writer.flush();
        }catch (Exception e2){
            JOptionPane.showMessageDialog(this,"发送退出棋盘请求失败："+e2.getMessage());
            return;
        }

        //4 如果退出的桌面正在对弈，应修改游戏界面内容
    }

    /**
     * 刷新列表处理函数，用于向服务器请求所有已经创建好的棋局
     */
    private void doDesktopRefresh(){//不需要等用户的接收信息
        //1 取得自己的名字
        String myName = this.textAccount.getText();
        String str = "RefreshDesktop;"+myName;
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(str);
            writer.newLine();
            writer.flush();
        }catch (Exception e2){
            JOptionPane.showMessageDialog(this,"发送刷新列表请求失败："+e2.getMessage());
            return;
        }
    }

    /**
     * 通过socket成员，发送聊天信息
     * @param content 发言区输入的字符串
     */
    public void sendTalk(String content){
        if(socket==null) return;
        if(chessboard.player2==null||chessboard.player2.equals("")){
            return;
        }
        /**发言信息要指定在哪一个棋局上发言*/
        String myName = this.textAccount.getText();
        String str = "talk;"+chessboard.player2+";"+chessboard.loginFrame.getAccount()+";"+content;

        /**发送聊天信息*/
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(str);
            writer.newLine();
            writer.flush();
        }catch (Exception e2){
            JOptionPane.showMessageDialog(this,"聊天信息发送失败："+e2.getMessage());
            return;
        }
    }

    /**
     * 发送游戏结果
     * @param result 1为黑方胜，0为白方胜
     */
    public void sendResult(int result){
        String myName;
        if(result==1) myName= chessboard.player2;
        else myName= chessboard.player1;
        String str = "endGame;"+myName+";"+result;

        /**发送结果信息*/
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(str);
            writer.newLine();
            writer.flush();
        }catch (Exception e2){
            JOptionPane.showMessageDialog(this,"发送对弈结果失败："+e2.getMessage());
            return;
        }
    }

    /**
     * 发送一个落子信息到服务器，服务器转发给所有参与者
     * @param curPieceColor
     * @param row
     * @param col
     */
    public void sendPiece(int curPieceColor, int row, int col){
        String myName = this.textAccount.getText();
        String str="Piece;"+chessboard.player2+";"+chessboard.loginFrame.getAccount()+";"+curPieceColor+";"+row+";"+col;
        /**发送落子信息*/
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(str);
            writer.newLine();
            writer.flush();
        }catch (Exception e2){
            JOptionPane.showMessageDialog(this,"发送落子信息失败："+e2.getMessage());
            return;
        }
    }

    /**
     * 发送悔棋消息到服务器，服务器转发给所有参与者
     */
    public void sendUndo(int curPieceColor,int row,int col){
        String myName = this.textAccount.getText();
        String str="Undo;"+chessboard.player2+";"+chessboard.loginFrame.getAccount()+";"+curPieceColor+";"+row+";"+col;;
        /**发送悔棋信息*/
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(str);
            writer.newLine();
            writer.flush();
        }catch (Exception e2){
            JOptionPane.showMessageDialog(this,"发送悔棋信息失败："+e2.getMessage());
            return;
        }
    }




}
