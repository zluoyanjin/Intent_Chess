import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Chessboard  extends JFrame implements ActionListener, WindowListener {
    public final static int WIDTH =880;
    public final static int HEIGHT =640;
    /**棋盘大小**/
    public final static int BOARD_SIZE=540;
    public final static int MARGIN =20;
    public final static int SPACING =BOARD_SIZE/18;
    /**棋盘面版大小**/
    public final static int BOARD_PANEL_SIZE=BOARD_SIZE+2*MARGIN;
    /**聊天面板宽度**/
    public final static int TALK_PANEL_WIDTH=300;
    /**玩家信息显示区域高度**/
    public final static int PLAYER_INFO_HEIGHT=100;
    /**发言区域高度**/
    public final static int SPEAKING_AREA_HEIGHT=25;
    /**聊天区域高度**/
    public final static int TALKING_AREA_HEIGHT=HEIGHT-SPEAKING_AREA_HEIGHT-PLAYER_INFO_HEIGHT;
    public final static int BLACK =1;
    public final static int WHITE =2;
    /**人人对弈**/
    public final static int GAME_TYPE_MAN_MAN =1;
    /**人机对弈**/
    public final static int GAME_TYPE_MAN_MACHINE =2;
    /**网络对弈**/
    public final static int GAME_TYPE_NETWORK =3;
    private  int[][] states=new int[19][19];
    private int curColor=BLACK;
    private int cnt=0;
    private JMenuBar menuBar;//菜单条
    private  JMenu gameMenu;//游戏菜单
    private JMenuItem menuItemMan_Man;//人人
    private JMenuItem menuItemMan_Machine;//人机
    /**网络对弈*/
    private JMenuItem menuItemNetwork;
    private JMenuItem menuItemUndo;//悔棋
    private JMenuItem menuSaveGame;//中局存盘
    private JMenuItem menuItemLoadGame;//读取存盘
    private  JMenuItem menuItemHistory;//历史战绩
    private JMenuItem menuItemExit;//退出程序
    /**棋盘面板*/
    private BoardPanel boardPanel = new BoardPanel();
    /**聊天面板*/
    private TalkPanel talkPanel = new TalkPanel();
    private int gamType= GAME_TYPE_MAN_MAN;
    public String player1=null;//玩家一姓名
    public String player2=null;//玩家二姓名
    private Judge jedge=new Judge();
    private  Robot robot=new Robot();
    private  int[] lastRow=new int[100];
    private  int[] lastCol=new int[100];
//    private  int[] lastWhiteRow=new int[19];
//
//    private  int[] lastWhiteCol=new int[19];
//    private int lastBlackRow,lastBlackCol;
//    private int lastWhiteRow,lastWhiteCol;
    private boolean started=false;
//    private int lastRow;
//    private int lastCol;
    /**登录窗口的引用*/
    public LoginFrame loginFrame;

    /**
     * 游戏开始函数
     */
    public void start(){
        this.reset();
        this.started=true;
        this.repaint();//刷新界面
    }

    /**
     * 游戏结束函数
     */
    public void stop(){
        this.started=false;
    }

    /**
     * 在网络对弈中用来重置玩家姓名
     * @param blackPlayer
     * @param whitePlayer
     */
    public void setPlayer(String blackPlayer,String whitePlayer){
        this.player1=blackPlayer;
        this.player2=whitePlayer;
        this.talkPanel.setLblPlayerName(player1,player2);
        this.repaint();
    }

    /**
     * 首先接触到发言信息的是Chessboard窗口，而非talkPanel
     * @param who
     * @param content
     */
    public void receiveTalk(String who,String content){
        this.talkPanel.showTalk(who,content);
    }


    /**
     * 网络对弈中落子函数
     * @param color
     * @param row
     * @param col
     */
    public void putPiece(int color, int row, int col){
        //如果远端落子情况与我当前落子颜色不一致，则什么也不做
        if(color!=this.curColor) return;
        //执行落子
        this.states[row][col]=color;
        cnt++;
        lastRow[cnt]=row;
        lastCol[cnt]=col;
        //判断输赢
        int result= jedge.doJudge(states);
        //如果有输赢结果，则向服务器发送结果
        if(result==BLACK){
            JOptionPane.showMessageDialog(this,"黑方胜利！");
            this.saveHistory(result);
            this.loginFrame.sendResult(result);
            this.stop();
        }else if(result==WHITE){
            JOptionPane.showMessageDialog(this,"白方胜利！");
            this.saveHistory(result);
            this.loginFrame.sendResult(result);
            this.stop();
        }
        //否则切换当前落子颜色
        else{
            if(this.curColor==BLACK) this.curColor=WHITE;
            else this.curColor=BLACK;
        }
        //刷新界面
        this.repaint();
    }

    /**
     * 初始化界面函数
     */
    public void initLayout(){
        Container pane =this.getContentPane();
        BorderLayout borderLayout=new BorderLayout();
        pane.setLayout(borderLayout);
        //界面放置
        pane.add(this.boardPanel,BorderLayout.CENTER);
        pane.add(this.talkPanel,BorderLayout.EAST);

        this.boardPanel.setPreferredSize(new Dimension(Chessboard.BOARD_PANEL_SIZE,0));
        this.talkPanel.setPreferredSize(new Dimension(Chessboard.TALK_PANEL_WIDTH,0));

    }

    public Chessboard(){
        //将窗口与鼠标监听器实现对象相关联
        createMenu();
        this.addWindowListener(this);
    }

//    public void paint(Graphics g){
//        super.paint(g);
//        Color bgColor =new Color(200,200,200);
//        g.setColor(bgColor);
//        g.fillRect(MARGIN,MARGIN,WIDTH-2*MARGIN,HEIGHT-2*MARGIN);
//        g.setColor(Color.BLACK);
//        for(int i=0;i<19;i++){
//            g.drawLine(MARGIN,MARGIN+i*SPACING,WIDTH-MARGIN,i*SPACING+MARGIN);
//            g.drawLine(MARGIN+i*SPACING,MARGIN,i*SPACING+MARGIN,HEIGHT-MARGIN);
//        }
//        for(int i=0;i<19;i++){
//            for(int j=0;j<19;j++){
//                if(this.states[i][j]==0)  continue;
//                if(this.states[i][j]==BLACK){
////                    System.out.println(1);
//                    g.setColor(Color.BLACK);
//                    int x=j*SPACING+MARGIN;
//                    int y=i*SPACING+MARGIN;
//                    g.fillOval(x-10,y-10,20,20);
//                }else{
//                    g.setColor(Color.WHITE);
//                    int x=j*SPACING+MARGIN;
//                    int y=i*SPACING+MARGIN;
//                    g.fillOval(x-10,y-10,20,20);
//                }
//            }
//        }
//        //显示玩家姓名
//        Font font=new Font("宋体",0,20);
//        if(this.player1!=null){
//            if(this.curColor==BLACK) g.setColor(Color.RED);
//            else g.setColor(Color.BLACK);
//            g.drawString(this.player1,40,110);
//        }
//        if(this.player2!=null){
//            if(this.curColor==WHITE) g.setColor(Color.RED);
//            else g.setColor(Color.BLACK);
//            g.drawString(this.player2,850,110);
//        }
//    }
    private  void createMenu(){
        this.menuBar=new JMenuBar();
        this.setJMenuBar(menuBar);
        //创建游戏菜单，并加入菜单条去
        gameMenu=new JMenu("游戏");
        this.menuBar.add(gameMenu);
        //创建所有菜单项，并加入游戏菜单中去
        menuItemMan_Man =new JMenuItem("人人对弈");
        menuItemMan_Man.addActionListener(this);
        gameMenu.add(menuItemMan_Man);
        menuItemMan_Machine = new JMenuItem("人机对弈");
        menuItemMan_Machine.addActionListener(this);
        gameMenu.add(menuItemMan_Machine);
        menuItemNetwork =new JMenuItem("网络对弈");
        menuItemNetwork.addActionListener(this);
        gameMenu.add(menuItemNetwork);
        gameMenu.addSeparator();//添加一个分割条

        menuItemUndo =new JMenuItem("悔棋");
        menuItemUndo.addActionListener(this);
        gameMenu.add(menuItemUndo);
        menuSaveGame=new JMenuItem("中局存盘");
        menuSaveGame.addActionListener(this);
        gameMenu.add(menuSaveGame);
        menuItemLoadGame=new JMenuItem("读取存盘");
        menuItemLoadGame.addActionListener(this);
        gameMenu.add(menuItemLoadGame);
        menuItemHistory=new JMenuItem("历史战绩");
        menuItemHistory.addActionListener(this);
        gameMenu.add(menuItemHistory);
        gameMenu.addSeparator();

        menuItemExit=new JMenuItem("退出程序");
        menuItemExit.addActionListener(this);//左边是监听器
        gameMenu.add(menuItemExit);
    }

    public static void main(String[] args){
        Chessboard board=new Chessboard();
        board.setTitle("五子棋");
        board.setSize(WIDTH,HEIGHT);
        board.initLayout();
        board.setLocationRelativeTo(null);//窗口居中
        board.setResizable(false);
        board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//EXIT_ON_CLOSE是静态变量,作用是关闭程序进程
        board.setVisible(true);//窗口显示
    }
//    public void paint(Graphics g){
//        super.paint(g);
//        drawPlyer(g);
//    }
//    private Font playerFont=new Font("宋体",0,14);
//    private void drawPlyer(Graphics g){
//        g.setFont(playerFont);
//        if(player1!=null){
//            if(curColor==BLACK) g.setColor(Color.RED);
//            else g.setColor(Color.BLACK);
//            g.drawString(player1,40,110);
//        }
//        if(player2!=null){
//            if(curColor==WHITE) g.setColor(Color.RED);
//            else g.setColor(Color.BLACK);
//            g.drawString(player2,850,110);
//        }
//    }

    //棋盘状态初始化
    public void reset(){
        for(int i=0;i<19;i++){
            for(int j=0;j<19;j++){
                this.states[i][j]=0;
                repaint();
            }
        }
        this.curColor=BLACK;
    }
//    @Override
//    public void mouseClicked(MouseEvent e) {//鼠标单击
//
//        int[] rowcol=this.pixel2board(e.getX(),e.getY());cnt++;
//        if(rowcol !=null){
//            int row=rowcol[0];
//            int col=rowcol[1];
//            this.states[row][col]=curColor;
//            if(curColor==BLACK){
//                this.lastBlackRow[cnt]=row;
//                this.lastBlackCol[cnt]=col;
//            }
//            else {
//                this.lastWhiteRow[cnt]=row;
//                this.lastWhiteCol[cnt]=col;
//            }
//            if(curColor==BLACK) curColor=WHITE;
//            else curColor=BLACK;
//            int result=jedge.doJudge(this.states);
//            if(result==1){
//                JOptionPane.showMessageDialog(this,"黑方胜利");
//                this.saveHistory(1);
//                reset();
//                return;
//
//            }
//            else if(result==2){
//                JOptionPane.showMessageDialog(this,"白方胜利");
//                this.saveHistory(2);
//                reset();
//                return;
//            }
//            if(gamType== GAME_TYPE_MAN_MACHINE){
//                int[] robotresult=robot.think(states,row,col);
//                cnt++;
//                int rrow=robotresult[0],rcol=robotresult[1];
//                this.states[rrow][rcol]=curColor;
//                curColor=BLACK;
//                this.lastWhiteRow[cnt]=rrow;
//                this.lastWhiteCol[cnt]=rcol;
//                result=jedge.doJudge(this.states);
//                if(result==1){
//                    JOptionPane.showMessageDialog(this,"黑方胜利");
//                    this.saveHistory(1);
//                    reset();
//                    return;
//                }
//                else if(result==2){
//                    JOptionPane.showMessageDialog(this,"白方胜利");
//                    this.saveHistory(2);
//                    reset();
//                    return;
//                }
//            }
//        }
//
//        //System.out.println(tempx+","+tempy);
//        this.repaint();
//
//    }
  //保存历史记录1为黑放胜，2为白方胜
    private void saveHistory(int i) {
        //生成对弈时间
        Date now=new Date();
        SimpleDateFormat ftm=new SimpleDateFormat("yyyy-MM--dd hh:mm:ss");
        String timeText =ftm.format(now);
        //生成待保存到文件中的文本
        String text=timeText+":"+player1+","+player2+":"+(i==1?"黑方胜":"白方胜");

        //异常处理快
        try{
        //将text保存到文件中去
        File file=new File("D:\\JavaPrj\\HELLOWORD\\history.txt");//定义文件对象
        FileWriter w=new FileWriter(file,true);//添加写
        //定义缓冲写函数
        BufferedWriter writer=new BufferedWriter(w);
        writer.write(text+"\n");
        writer.close();
        }catch (Exception e){
            System.out.println("保存历史战绩失败了，失败原因："+e.getMessage());
        }
    }
//
//    @Override
//    public void mousePressed(MouseEvent e) {//鼠标进入
//
//    }
//
//    @Override
//    public void mouseReleased(MouseEvent e) {//鼠标离开
//
//    }
//
//    @Override
//    public void mouseEntered(MouseEvent e) {//鼠标按下
//
//    }
//
//    @Override
//    public void mouseExited(MouseEvent e) {//鼠标释放
//
//    }

    private  int[] pixel2board(int x,int y){
        if(x < MARGIN || x > MARGIN + SPACING*18)
            return null;
        if(y < MARGIN || y > MARGIN + SPACING*18)
            return null;
        int col=(x-MARGIN)/SPACING;
        int t=(x-MARGIN)%SPACING;
        if(t>SPACING/2) col+=1;
        int row=(y-MARGIN)/SPACING;
        t=(y-MARGIN)%SPACING;
        if(t>SPACING/2) row+=1;
        return new int[]{row,col};
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==menuItemExit){
            int r=JOptionPane.showConfirmDialog(this,"系统将退出，确认？","提醒",JOptionPane.OK_CANCEL_OPTION);
            if(r==JOptionPane.OK_OPTION){
                this.dispose();
            }

        }else if(e.getSource()== menuItemMan_Man){
            this.gamType= GAME_TYPE_MAN_MAN;
            this.player1=JOptionPane.showInputDialog("请输入玩家一的姓名");
            this.player2=JOptionPane.showInputDialog("请输入玩家二的姓名");
            this.talkPanel.setLblPlayerName(player1,player2);
            this.start();
        }else if(e.getSource()== menuItemMan_Machine){
            this.gamType= GAME_TYPE_MAN_MACHINE;
            this.player1=JOptionPane.showInputDialog("请输入玩家一的姓名");
            this.player2="机器人";
            this.talkPanel.setLblPlayerName(player1,player2);
            this.start();
        }
        else if(e.getSource()==menuItemNetwork){
            this.gamType=Chessboard.GAME_TYPE_NETWORK;

            if(this.loginFrame==null) this.loginFrame=new LoginFrame(this);
            this.loginFrame.setVisible(true);
        }
        else if(e.getSource()==menuItemUndo){
            if(this.lastCol[cnt]<0|| this.lastRow[cnt]<0) return;
            if(gamType==GAME_TYPE_NETWORK){
                loginFrame.sendUndo(this.curColor,this.lastRow[cnt],this.lastCol[cnt]);
            }
            else if (gamType==GAME_TYPE_MAN_MAN){
                this.states[this.lastRow[cnt]][this.lastCol[cnt]]=0;
                cnt--;
                if(curColor==BLACK)curColor=WHITE;
                else curColor=BLACK;
                this.repaint();
            }else if (gamType==GAME_TYPE_MAN_MACHINE){
                this.states[lastRow[cnt]][lastCol[cnt]]=0;
                cnt--;
                this.states[lastRow[cnt]][lastCol[cnt]]=0;
                cnt--;
                curColor=BLACK;
                this.repaint();
            }

        }
        else if(e.getSource()==menuItemHistory){//历史菜单被点击了
            String content="",line="";
            try{
            File file=new File("D:\\JavaPrj\\HELLOWORD\\history.txt");
            FileReader r=new FileReader(file);
            BufferedReader reader=new BufferedReader(r);

            while(true){
                line =reader.readLine();
                if(line==null) break;
                content+=line+"\n";
            }
            reader.close();

            }catch (Exception ex){
                System.out.println("读取历史战绩失败:"+ex.getMessage());
                JOptionPane.showMessageDialog(this,"读取历史战绩失败:"+ex.getMessage());
            }
            JOptionPane.showMessageDialog(this,content);
        }
        else if(e.getSource()==menuSaveGame){
            this.saveBoardState();
        }
        else if(e.getSource()==menuItemLoadGame){
            this.loadBoardState();
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }
    //当窗口将要被关闭时，Java调用该函数
    @Override
    public void windowClosing(WindowEvent e) {
       int r=JOptionPane.showConfirmDialog(this,"系统将退出，确认？","提醒",JOptionPane.OK_CANCEL_OPTION);
       if(r==JOptionPane.CANCEL_OPTION){
           this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);//取消关闭

       }
       else {
           this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//关闭系统
       }
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
    //读取所有对应状态
    private void loadBoardState(){
        try{
            File file =new File("D:\\JavaPrj\\HELLOWORD\\board.txt");
            FileReader r=new FileReader(file);
            BufferedReader reader=new BufferedReader(r);
            String line =reader.readLine();
            this.gamType=Integer.parseInt(line);
            this.player1=reader.readLine();
            this.player2=reader.readLine();
            line =reader.readLine();
            this.curColor=Integer.parseInt(line);
            //读取棋盘状态
            for(int i=0;i<19;i++){
                line =reader.readLine();
                String[] column=line.split(",");
                for(int j=0;j<19;j++){
                   this.states[i][j]=Integer.parseInt(column[j]);
                }
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(this,"读取棋盘状态出错："+e.getMessage());
        }
        //刷新界面
        this.repaint();
    }
    //保存所有对应状态
    private  void saveBoardState(){
        try{
            File file =new File("D:\\JavaPrj\\HELLOWORD\\board.txt");
            FileWriter w=new FileWriter(file);
            BufferedWriter writer=new BufferedWriter(w);
            //写对应类型
            writer.write(this.gamType+"\n");
            //写名字
            writer.write(player1+"\n");
            writer.write(player2+"\n");
            //写当前落子颜色
            writer.write(this.curColor+"\n");
            //写棋盘状态
            StringBuffer buf=new StringBuffer();//当字符串频繁发生变化，则不能使用string，而应该使用StringBuffer
            for(int i=0;i<19;i++){
                for(int j=0;j<19;j++){
                    if(j<18) buf.append(this.states[i][j]+",");
                    else buf.append(this.states[i][j]);
                }
                buf.append("\n");
            }
            writer.write(buf.toString());
            writer.close();
        }catch (Exception e){
            JOptionPane.showMessageDialog(this,"中盘存放出错："+e.getMessage());
        }

    }

    public void Undo(int color,int row,int col) {
        if(color!=this.curColor) return;
        this.states[row][col]=0;
        this.repaint();
        if(this.curColor==BLACK) this.curColor=WHITE;
        else this.curColor=BLACK;
        cnt--;
    }

    public void init() {
        for (int i=0;i<19;i++)
            for (int j=0;j<19;j++)
                states[i][j]=0;
        repaint();
    }


    /**
     * 用于显示棋盘的区域，继承JPanel,专门画棋盘
     * BoardPanel是Chessboard的内部类
     */
     class BoardPanel extends JPanel implements MouseListener{

            public BoardPanel() {
                this.addMouseListener(this);
            }

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g); // 调用父类的paintComponent方法以绘制组件的边界

                // 绘制棋盘线条
                g.setColor(Color.BLACK);
                for (int i = 0; i < 19; i++) {
                    g.drawLine(MARGIN, MARGIN + i * SPACING, BOARD_PANEL_SIZE - MARGIN, MARGIN + i * SPACING);
                    g.drawLine(MARGIN + i * SPACING, MARGIN, MARGIN + i * SPACING, BOARD_PANEL_SIZE - MARGIN);
                }

                // 绘制棋子
                for (int i = 0; i < 19; i++) {
                    for (int j = 0; j < 19; j++) {
                        if (states[i][j] == 0) continue;
                        if (states[i][j] == BLACK) {
                            g.setColor(Color.BLACK);
                        } else {
                            g.setColor(Color.WHITE);
                        }
                        int x = j * SPACING + MARGIN;
                        int y = i * SPACING + MARGIN;
                        g.fillOval(x - 10, y - 10, 20, 20);
                    }
                }
            }



        @Override
        public void mouseClicked(MouseEvent e) {//鼠标单击
            if(!Chessboard.this.started) return;

            if (gamType==Chessboard.GAME_TYPE_NETWORK){
                String myName=loginFrame.getAccount();
                if(!myName.equals(player1)&&!myName.equals(player2)){
                    return;
                }
                else if(myName.equals(player1)&&curColor==Chessboard.BLACK){
                    return;
                }
                else if(myName.equals(player2)&&curColor==Chessboard.WHITE){
                    return;
                }

            }
            int[] rowcol=pixel2board(e.getX(),e.getY());
            if(rowcol !=null){
                int row=rowcol[0];
                int col=rowcol[1];
                states[row][col]=curColor;
                cnt++;
                lastRow[cnt]=row;
                lastCol[cnt]=col;
                if (gamType!=GAME_TYPE_NETWORK){
                    if(curColor==BLACK) curColor=WHITE;
                    else curColor=BLACK;

                }
                int result;
                if(gamType==GAME_TYPE_MAN_MAN){
                    result=jedge.doJudge(states);
                    if(result==1){
                        JOptionPane.showMessageDialog(this,"黑方胜利");
                        saveHistory(1);
                        reset();
                        return;

                    }
                    else if(result==2){
                        JOptionPane.showMessageDialog(this,"白方胜利");
                        saveHistory(2);
                        reset();
                        return;
                    }
                }
                else if (gamType == GAME_TYPE_MAN_MACHINE) {
                        int[] robotresult = robot.think(states, row, col);
                        cnt++;
                        int rrow = robotresult[0], rcol = robotresult[1];
                        states[rrow][rcol] = curColor;
                        curColor = BLACK;
                        lastRow[cnt] = rrow;
                        lastCol[cnt] = rcol;
                        result = jedge.doJudge(states);
                        if (result == 1) {
                            JOptionPane.showMessageDialog(this, "黑方胜利");
                            saveHistory(1);
                            reset();
                            return;
                        } else if (result == 2) {
                            JOptionPane.showMessageDialog(this, "白方胜利");
                            saveHistory(2);
                            reset();
                            return;
                        }
                } else if (gamType == GAME_TYPE_NETWORK) {
                        loginFrame.sendPiece(curColor, row, col);
                        if(curColor==BLACK) curColor=WHITE;
                        else curColor=BLACK;
                }

            }

            //System.out.println(tempx+","+tempy);
            repaint();
            talkPanel.repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {//鼠标进入

        }

        @Override
        public void mouseReleased(MouseEvent e) {//鼠标离开

        }

        @Override
        public void mouseEntered(MouseEvent e) {//鼠标按下

        }

        @Override
        public void mouseExited(MouseEvent e) {//鼠标释放

        }
    }

    /**
     * 用于显示聊天信息的区域，也是内部类
     */
    class TalkPanel extends  JPanel implements KeyListener{
        /**第一个玩家名称显示组件*/
        private JLabel lblPlayer1Name=new JLabel();
        /**第二个玩家名称显示组件*/
        private JLabel lblPlayer2Name=new JLabel();
        /**两个玩家组件显示在这个面板上*/
        private JPanel playerPanel=new JPanel();
        /**发言显示区，支持多行*/
        private JTextArea infoField=new JTextArea();
        /**发言区，只能多行*/
        private JTextField speakField=new JTextField();
        /**
         * 构造函数
         */
         public TalkPanel(){
             /**设置滚动条*/
             JScrollPane scroll =new JScrollPane(infoField);
             infoField.setWrapStyleWord(true);
             infoField.setCaretPosition(infoField.getText().length());
             scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
             scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
             BorderLayout borderLayout1=new BorderLayout();
             this.setLayout(borderLayout1);

             this.add(playerPanel, BorderLayout.NORTH);
             this.add(scroll,BorderLayout.CENTER);
             this.add(this.speakField,BorderLayout.SOUTH);

             this.playerPanel.setPreferredSize(new Dimension(0,Chessboard.PLAYER_INFO_HEIGHT));
             this.speakField.setPreferredSize(new Dimension(0,Chessboard.SPEAKING_AREA_HEIGHT));

             this.infoField.setEditable(false);//不让编辑

             this.playerPanel.setLayout(null);
             this.playerPanel.add(lblPlayer1Name);
             this.playerPanel.add(lblPlayer2Name);
             this.lblPlayer2Name.setBounds(25,30,50,20);
             this.lblPlayer1Name.setBounds(180,30,50,20);

             this.speakField.addKeyListener(this);//加上监听器
         }

        /**
         * 显示玩家1和玩家2姓名
         * @param p1
         * @param p2
         */
         public void setLblPlayerName(String p1,String p2){
             this.lblPlayer1Name.setText(p1);
             this.lblPlayer2Name.setText(p2);
         }

         public void paintComponent(Graphics g){
             super.paintComponent(g);
             if(player1!=null){
                 if(curColor==Chessboard.BLACK)
                     this.lblPlayer2Name.setForeground(Color.RED);
                 else
                     this.lblPlayer2Name.setForeground(Color.BLACK);
             }
             if(player2!=null) {
                 if (curColor == Chessboard.WHITE)
                     this.lblPlayer1Name.setForeground(Color.RED);
                 else
                     this.lblPlayer1Name.setForeground(Color.BLACK);
             }
         }

        /**
         * 显示发言信息（通过网络服务器）
         * @param who
         * @param content
         */
         public void showTalk(String who,String content){
             String text = this.infoField.getText();
             text+="\r\n"+who+"\r\n"+content+"\r\n";
             this.infoField.setText(text);
         }


        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
             /**发现是回车*/
             if(e.getKeyCode()==0x0A){
                 if(loginFrame==null)return;//判断有无登录过
                 if(Objects.equals(this.speakField.getText(), "")) return;
                 loginFrame.sendTalk(this.speakField.getText());
                 this.speakField.setText("");//发言内容初始为空
             }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
    }
}

