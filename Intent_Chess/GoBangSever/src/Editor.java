import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// 编辑窗口类，继承自 JDialog，并实现 ActionListener 接口
public class Editor extends JDialog implements ActionListener {
    private JTextField txtName; // 姓名文本框
    private JPasswordField passwordField; // 密码文本框
    private JTextField txtCount; // 游戏次数文本框
    private JTextField txtScore; // 积分文本框
    private JTextField txtState; // 状态文本框
    private JButton btnOk; // 确定按钮
    private JButton btnCancel; // 取消按钮

    private boolean cancled; // 是否取消标志

    private Player player; // 玩家对象

    // 构造函数，传入父窗口和玩家对象
    public Editor(JFrame parent, Player player) {
        this.setTitle("编辑窗口"); // 设置窗口标题

        this.setSize(291, 300); // 设置窗口大小
        this.setModal(true); // 设置为模态对话框

        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(parent); // 设置窗口居中显示
        getContentPane().setLayout(null); // 使用空布局

        JLabel lblNewLabel = new JLabel("玩家姓名"); // 创建姓名标签
        lblNewLabel.setBounds(22, 21, 73, 15); // 设置标签位置和大小
        getContentPane().add(lblNewLabel); // 将标签添加到内容面板

        txtName = new JTextField(); // 创建姓名文本框
        txtName.setBounds(96, 18, 144, 21); // 设置文本框位置和大小
        getContentPane().add(txtName); // 将文本框添加到内容面板
        txtName.setColumns(10); // 设置文本框列数

        JLabel lblNewLabel_1 = new JLabel("玩家口令"); // 创建口令标签
        lblNewLabel_1.setBounds(22, 54, 73, 15); // 设置标签位置和大小
        getContentPane().add(lblNewLabel_1); // 将标签添加到内容面板

        JLabel lblNewLabel_2 = new JLabel("游戏次数"); // 创建游戏次数标签
        lblNewLabel_2.setBounds(22, 88, 73, 15); // 设置标签位置和大小
        getContentPane().add(lblNewLabel_2); // 将标签添加到内容面板

        txtCount = new JTextField(); // 创建游戏次数文本框
        txtCount.setColumns(10); // 设置文本框列数
        txtCount.setBounds(96, 85, 144, 21); // 设置文本框位置和大小
        getContentPane().add(txtCount); // 将文本框添加到内容面板

        JLabel lblNewLabel_3 = new JLabel("玩家积分"); // 创建积分标签
        lblNewLabel_3.setBounds(22, 128, 73, 15); // 设置标签位置和大小
        getContentPane().add(lblNewLabel_3); // 将标签添加到内容面板

        txtScore = new JTextField(); // 创建积分文本框
        txtScore.setColumns(10); // 设置文本框列数
        txtScore.setBounds(96, 125, 144, 21); // 设置文本框位置和大小
        getContentPane().add(txtScore); // 将文本框添加到内容面板

        JLabel lblNewLabel_4 = new JLabel("玩家状态"); // 创建状态标签
        lblNewLabel_4.setBounds(22, 167, 73, 15); // 设置标签位置和大小
        getContentPane().add(lblNewLabel_4); // 将标签添加到内容面板

        txtState = new JTextField(); // 创建状态文本框
        txtState.setColumns(10); // 设置文本框列数
        txtState.setBounds(96, 164, 144, 21); // 设置文本框位置和大小
        getContentPane().add(txtState); // 将文本框添加到内容面板

        passwordField = new JPasswordField(); // 创建密码文本框
        passwordField.setBounds(96, 49, 144, 21); // 设置文本框位置和大小
        getContentPane().add(passwordField); // 将文本框添加到内容面板

        btnOk = new JButton("确定", new ImageIcon(this.getClass().getResource("/images/ok16.png"))); // 创建确定按钮
        btnOk.setBounds(22, 212, 93, 23); // 设置按钮位置和大小
        getContentPane().add(btnOk); // 将按钮添加到内容面板

        btnCancel = new JButton("取消", new ImageIcon(this.getClass().getResource("/images/cancel16.png"))); // 创建取消按钮
        btnCancel.setBounds(149, 212, 93, 23); // 设置按钮位置和大小
        getContentPane().add(btnCancel); // 将按钮添加到内容面板

        this.player = player; // 初始化玩家对象
        showPlayerInfo(); // 显示玩家信息

        this.btnOk.addActionListener(this); // 添加确定按钮的监听器
        this.btnCancel.addActionListener(this); // 添加取消按钮的监听器
    }

    // 显示玩家信息的方法
    private void showPlayerInfo() {
        // 如果玩家对象为空
        if (this.player == null) {
            // 清空所有文本框和密码框
            this.txtCount.setText("");
            this.txtName.setText("");
            this.passwordField.setText("");
            this.txtScore.setText("");
            this.txtState.setText("");
            return; // 结束方法
        }
        // 否则，将玩家信息显示在文本框和密码框中
        this.txtCount.setText(player.getCount() + "");
        this.txtName.setText(player.getCaption());
        this.passwordField.setText(player.getPassword());
        this.txtScore.setText(player.getScore() + "");
        this.txtState.setText(player.getOnline());
    }

    // 主方法，用于测试编辑窗口
    public static void main(String[] args) {
        // 创建编辑窗口对象并设置为可见
        new Editor(null, null).setVisible(true);
    }

    // 监听器的 actionPerformed 方法
    @Override
    public void actionPerformed(ActionEvent e) {
        // 如果事件源是取消按钮
        if (e.getSource() == btnCancel) {
            // 弹出确认对话框，询问是否放弃修改
            int result = JOptionPane.showConfirmDialog(this, "放弃修改，确认？");
            // 如果用户确认放弃修改
            if (result == JOptionPane.OK_OPTION) {
                this.setVisible(false); // 隐藏编辑窗口
                cancled = true; // 设置取消标志为 true
            }
        }
        // 如果事件源是确定按钮
        else if (e.getSource() == btnOk) {
            // 如果玩家对象为空
            if (this.player == null)
                // 创建新的玩家对象，并使用文本框和密码框中的内容初始化
                this.player = new Player(this.txtName.getText(), new String(this.passwordField.getPassword()),
                        Integer.parseInt(this.txtCount.getText()), Integer.parseInt(this.txtScore.getText()),
                        this.txtState.getText());
            else {
                // 否则，更新玩家对象的信息
                this.player.setCaption(this.txtName.getText());
                this.player.setPassword(new String(this.passwordField.getPassword()));
                this.player.setCount(Integer.parseInt(this.txtCount.getText()));
                this.player.setScore(Integer.parseInt(this.txtScore.getText()));
                this.player.setOnline(this.txtState.getText());
            }
            cancled = false; // 设置取消标志为 false
            this.setVisible(false); // 隐藏编辑窗口
        }
    }

    // 获取取消标志的方法
    public boolean isCancled() {
        return cancled;
    }

    // 获取玩家对象的方法
    public Player getPlayer() {
        return player;
    }
}

//1编辑器
//未做非空校验
//取消逻辑有错：可以直接关闭窗口，仍以为新增
//口令二次验证，加密
//2.主窗口
//翻页
//下方显示总记录数
