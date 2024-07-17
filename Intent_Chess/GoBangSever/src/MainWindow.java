import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame implements ActionListener {
    private JToolBar toolBar; // 工具栏
    private JButton btnAdd; // 添加按钮
    private JButton btnUpdate; // 更新按钮
    private JButton btnDelete; // 删除按钮
    private JTable table; // 表格

    private JPanel queryPanel; // 查询面板
    private JLabel lblName; // 姓名标签
    private JTextField txtName; // 姓名文本框
    private JLabel lblScore; // 分数标签
    private JComboBox comConditions; // 查询条件下拉框
    private JTextField txtScore; // 分数文本框
    private JButton btnQuery; // 查询按钮
    private JCheckBox ckRough; // 模糊匹配复选框


    public MainWindow() {
        initWorkPanel(); // 初始化工作面板
        initQueryPanel(); // 初始化查询面板

        loadPlayerFile(); // 加载玩家文件

        this.setSize(600, 450); // 设置窗口大小
        this.setLocationRelativeTo(null); // 将窗口居中显示
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置关闭操作
    }

    // 初始化工作面板
    private void initWorkPanel() {
        JPanel pane = (JPanel) this.getContentPane(); // 获取内容面板
        pane.setLayout(new BorderLayout(0, 0)); // 使用边界布局

        JPanel workareaPanel = new JPanel(); // 工作区面板
        pane.add(workareaPanel, BorderLayout.CENTER); // 添加到内容面板中
        workareaPanel.setLayout(new BorderLayout(0, 0)); // 使用边界布局

        toolBar = new JToolBar(); // 工具栏
        workareaPanel.add(toolBar, BorderLayout.NORTH); // 添加到工作区面板的北部

        // 添加按钮和图标
        btnAdd = new JButton("新建", new ImageIcon(this.getClass().getResource("/images/add16.png")));
        btnUpdate = new JButton("修改", new ImageIcon(this.getClass().getResource("/images/update16.png")));
        btnDelete = new JButton("删除", new ImageIcon(this.getClass().getResource("/images/delete16.png")));
        toolBar.add(btnAdd); // 添加到工具栏
        toolBar.add(btnUpdate);
        toolBar.add(btnDelete);

        // 表格列标题
        String[] columns = new String[] { "玩家姓名", "玩家口令", "游戏次数", "玩家积分", "玩家状态̬" };
        DefaultTableModel model = new DefaultTableModel(null, columns); // 创建表格模型
        table = new JTable(model); // 创建表格
        JScrollPane jp = new JScrollPane(table); // 创建带滚动条的面板
        workareaPanel.add(jp, BorderLayout.CENTER); // 将表格添加到工作区面板的中部

        // 添加按钮监听器
        btnAdd.addActionListener(this);
        btnUpdate.addActionListener(this);
        btnDelete.addActionListener(this);
    }

    // 初始化查询面板
    private void initQueryPanel() {
        JPanel pane = (JPanel) this.getContentPane(); // 获取内容面板
        queryPanel = new JPanel(); // 创建查询面板
        pane.add(queryPanel, BorderLayout.NORTH); // 将查询面板添加到内容面板的北部

        // 创建查询面板的组件
        lblName = new JLabel("玩家姓名");
        txtName = new JTextField();
        lblScore = new JLabel("玩家积分");
        comConditions = new JComboBox();
        comConditions.addItem("相等");
        comConditions.addItem("大于");
        comConditions.addItem("小于");
        txtScore = new JTextField();
        btnQuery = new JButton("查询");
        ckRough = new JCheckBox("模糊匹配");

        queryPanel.setLayout(new GridLayout(1, 7)); // 设置查询面板的布局为网格布局
        // 添加组件到查询面板
        queryPanel.add(lblName);
        queryPanel.add(txtName);
        queryPanel.add(lblScore);
        queryPanel.add(comConditions);
        queryPanel.add(txtScore);
        queryPanel.add(btnQuery);
        queryPanel.add(ckRough);

        btnQuery.addActionListener(this); // 添加查询按钮的监听器
    }

    // 加载玩家文件
    private void loadPlayerFile() {
        URL url = this.getClass().getResource("/player.csv"); // 获取资源文件的URL

        FileReader fr = null;
        BufferedReader reader = null;
        try {
            fr = new FileReader(url.getFile()); // 创建文件读取流
            reader = new BufferedReader(fr);
            String line = null;
            // 逐行读取文件内容，初始化玩家列表
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals(""))
                    continue;
                String[] ss = line.split(",");
                String state = "离线";
                if (ss.length >= 5)
                    state = ss[4];

                Player player = new Player(ss[0], ss[1], Integer.parseInt(ss[2]), Integer.parseInt(ss[3]), state);

                GameLoggy.getInstance().players.put(player.getCaption(),player); // 将玩家对象添加到玩家列表中
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "读取玩家数据文件出错：" + e.getMessage());
        } finally {
            try {
                if (reader != null)
                    reader.close(); // 关闭文件读取流
            } catch (Exception e) {
            }
        }

        showPlayer(); // 显示玩家信息到表格中
    }

    // 保存玩家文件
    public void savePlayerFile() {
        URL url = this.getClass().getResource("/player.csv"); // 获取资源文件的URL

        FileWriter fr = null;
        BufferedWriter writer = null;
        try {
            fr = new FileWriter(url.getFile()); // 创建文件写入流
            writer = new BufferedWriter(fr);
            List<Player> players = GameLoggy.getInstance().toPlayerList();
            // 遍历玩家列表，将玩家信息写入文件
            for (Player player : players) {
                String line = player.toString();
                writer.write(line);
                writer.newLine();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "保存玩家数据文件出错：" + e.getMessage());
        } finally {
            try {
                if (writer != null)
                    writer.close(); // 关闭文件写入流
            } catch (Exception e) {
            }
        }
    }

    // 显示玩家信息到表格中
    private void showPlayer() {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel(); // 获取表格模型
        tableModel.setRowCount(0); // 清空表格数据
        List<Player> players = GameLoggy.getInstance().toPlayerList();
        // 遍历玩家列表，添加符合条件的玩家信息到表格中
        for (int i = 0; i < players.size(); i++) {
            if (!this.isMatchVisibleCondition(players.get(i)))
                continue;
            tableModel.addRow(new Object[] { players.get(i).getCaption(), players.get(i).getPassword(),
                   players.get(i).getCount(), players.get(i).getScore(), players.get(i).getOnline() });
        }
        table.invalidate(); // 使表格失效，刷新表格显示
    }

    // 判断玩家是否满足可见条件
    private boolean isMatchVisibleCondition(Player player) {
        if (!this.txtName.getText().equals("")) {
            if (!this.ckRough.isSelected()) {
                if (!player.getCaption().equals(this.txtName.getText()))
                    return false;
            } else {
                if (!player.getCaption().contains(this.txtName.getText()))
                    return false;
            }
        }

        if (!this.txtScore.getText().equals("")) {
            int s = Integer.parseInt(this.txtScore.getText());
            if (this.comConditions.getSelectedIndex() == 0) {
                if (s != player.getScore())
                    return false;
            } else if (this.comConditions.getSelectedIndex() == 1) {
                if (s > player.getScore())
                    return false;
            } else if (this.comConditions.getSelectedIndex() == 2) {
                if (s < player.getScore())
                    return false;
            }
        }
        return true;
    }

    // 主函数入口
    public static void main(String[] args) {
        MainWindow win = new MainWindow(); // 创建主窗口对象
        win.setVisible(true); // 设置主窗口可见
    }

    @Override
    // 按钮事件监听处理
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.btnQuery) { // 查询按钮事件处理
            this.showPlayer(); // 根据查询条件显示玩家信息
        } else if (e.getSource() == this.btnAdd) { // 新建按钮事件处理
            Editor editor = new Editor(this, null); // 创建编辑器窗口对象
            editor.setVisible(true); // 显示编辑器窗口
            if (editor.isCancled())
                return;
            Player p = editor.getPlayer();
            GameLoggy.getInstance().players.put(p.getCaption(),p);// 添加新玩家到玩家列表
            this.savePlayerFile(); // 保存玩家数据文件
            this.showPlayer(); // 刷新显示玩家信息
        } else if (e.getSource() == this.btnUpdate) { // 修改按钮事件处理
            DefaultTableModel tableModel = (DefaultTableModel) table.getModel(); // 获取表格模型
            int selectedRowIndex = table.getSelectedRow(); // 获取选择的行索引
            String name = (String) tableModel.getValueAt(selectedRowIndex, 0); // 获取选中行的姓名
            List<Player> players = GameLoggy.getInstance().toPlayerList();
            for (int i = 0; i < players.size(); i++) { // 遍历玩家列表
                if (players.get(i).getCaption().equals(name)) { // 找到要修改的玩家
                    Editor editor = new Editor(this, players.get(i)); // 创建编辑器窗口对象
                    editor.setVisible(true); // 显示编辑器窗口
                    if (editor.isCancled())
                        return;
                    this.savePlayerFile(); // 保存玩家数据文件
                    this.showPlayer(); // 刷新显示玩家信息
                    return;
                }
            }
        } else if (e.getSource() == this.btnDelete) { // 删除按钮事件处理
            DefaultTableModel tableModel = (DefaultTableModel) table.getModel(); // 获取表格模型
            int selectedRowIndex = table.getSelectedRow(); // 获取选择的行索引
            String name = (String) tableModel.getValueAt(selectedRowIndex, 0); // 获取选中行的姓名
            GameLoggy.getInstance().players.remove(name);// 删除玩家
            this.savePlayerFile(); // 保存玩家数据文件
            this.showPlayer(); // 刷新显示玩家信息
//            for (int i = 0; i < players.size(); i++) { // 遍历玩家列表
//                if (players.get(i).getCaption().equals(name)) { // 找到要删除的玩家
//                    int r = JOptionPane.showConfirmDialog(this, "该记录将被永久删除，确认？"); // 提示确认删除对话框
//                    if (r == JOptionPane.OK_OPTION) { // 用户确认删除
//                        players.remove(i); // 删除玩家
//                        this.savePlayerFile(); // 保存玩家数据文件
//                        this.showPlayer(); // 刷新显示玩家信息
//                    }
//                    return;
//                }
//            }
        }
    }

    /**
     * 根据传入的对象，修改界面
     * @param player
     */
    public void updatePlayer(Player player){
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        int rows=tableModel.getRowCount();
        for(int i=0;i<rows;i++){
            String name =(String)tableModel.getValueAt(i,0);
            if(player.getCaption().equals(name)){
                tableModel.setValueAt(player.getOnline(),i,4);
                return;
            }
        }
    }
}
