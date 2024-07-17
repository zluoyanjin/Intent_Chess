import java.util.ArrayList;
import java.util.List;

/**
 * 棋局信息，实体类
 */
public class Desktop {
    public String creatorName="";
    public String opponentName="";
    public List<String> byStandNames= new ArrayList<String >();

    /**
     * 构造函数
     * @param creatorName
     */
    public Desktop(String creatorName){
        this.creatorName=creatorName;
    }

    /**
     * 重写转换字符串函数
     * @return
     */
    @Override
    public String toString(){
        StringBuffer str =new StringBuffer();
        str.append(this.creatorName+";"+this.opponentName+";");
        for (int i=0;i<byStandNames.size();i++){
            if (i>0)str.append(",");
            str.append(byStandNames.get(i));
        }
        return str.toString();
    }

    /**
     * 只将旁观者连接成字符串
     * @return
     */
    public String getAllByStandName(){
        StringBuffer str =new StringBuffer();

        for (int i=0;i<byStandNames.size();i++){
            if (i>0)str.append(",");
            str.append(byStandNames.get(i));
        }
        return str.toString();
    }

    /**
     * 加入旁观者
     */
    public void putByStandName(String name){
        if(!byStandNames.contains(name)) byStandNames.add(name);
    }



}
