package server;

import enu.ClientMenu;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class ServerInteractiveUitl {

    private static ConcurrentHashMap<String, ChannelHandlerContext> onlineList=new ConcurrentHashMap<>();//��ǰ�����û����߳��б�
    private static ServerView serverView;


    private ServerInteractiveUitl(){}

    public static void setServerView(ServerView serverView) {
        ServerInteractiveUitl.serverView = serverView;
    }

    public static void requestConnection(ChannelHandlerContext channel ,String message){
        String name = message.substring(18);// ��ȡ�û��ǳ�
        if (onlineList.get(name) != null) {// �������
            channel.writeAndFlush(ClientMenu.REPEAT_NAME);
        }else {
            onlineList.put(name,channel);// ����ͨ����ӵ������б�
            reUsersList();// ˢ�·�����������Ϣ�б�

            String nickListMsg = "";

            Enumeration<String> keys = onlineList.keys();
            while(keys.hasMoreElements()){
                nickListMsg = nickListMsg + "," + keys.nextElement();// �������б�����ס��֯���ַ���
            }

            nickListMsg = ClientMenu.RE_USERS_LIST + nickListMsg;

            keys = onlineList.keys();
            ChannelHandlerContext satTemp;
            while(keys.hasMoreElements()){
                satTemp = onlineList.get(keys.nextElement());
                satTemp.writeAndFlush(nickListMsg);// �����µ��б���Ϣ���͵������ͻ���
            }
        }
    }

    public static void launchChallenge(String message) {
        String[] names = message.split("-");// ��ȡ�û��ǳ�

        ChannelHandlerContext channel = onlineList.get(names[2]);
        if(channel != null){//�����Ϊ�յĻ�������ս��Ϣ
            channel.writeAndFlush(ClientMenu.LAUNCH_CHALLENGE + names[1]);
        }else{//���Ϊ�յĻ��ط�������Ϣ
            channel = onlineList.get(names[1]);
            channel.writeAndFlush(ClientMenu.USER_EXIT + names[2]);
        }
    }


    public static void acceptChallenge(String message){
        String []names = message.split("-");

        ChannelHandlerContext channel = onlineList.get(names[2]);
        //��ͻ��˷��ͽ�����ս����Ϣ
        if(channel != null){
            channel.writeAndFlush(ClientMenu.ACCEPT_CHALLENGE + names[1]);
        }else{//���Ϊ�յĻ��ط�������Ϣ
            channel = onlineList.get(names[1]);
            channel.writeAndFlush(ClientMenu.USER_EXIT + names[2]);
        }
    }


    public static void refuseChallenge(String message){
        String []names = message.split("-");
        ChannelHandlerContext channel = onlineList.get(names[2]);
        if(channel != null){
            channel.writeAndFlush(ClientMenu.REFUSE_CHALLENGE + names[1]);
        }else{//���Ϊ�յĻ��ط�������Ϣ
            channel = onlineList.get(names[1]);
            channel.writeAndFlush(ClientMenu.USER_EXIT + names[2]);
        }
    }

    public static void admitDefeat(String message){
        String name = message.substring(12);
        ChannelHandlerContext channel = onlineList.get(name);
        channel.writeAndFlush(ClientMenu.ADMIT_DEFEAT);
    }

    public static void pieceMove(String message){
        String name = message.substring(5).split(",")[0];
        ChannelHandlerContext channel = onlineList.get(name);
        channel.writeAndFlush(ClientMenu.MOVE + message);
    }

    public static void userExit(String message){
        String [] names = message.split("-");
        if(names.length >= 3){//���name��Ϊ�յĻ�����ַ���������Ϣ
            onlineList.get(names[2]).writeAndFlush(ClientMenu.OPPONENT_EXIT);
        }

        onlineList.remove(names[1]);
        reUsersList();

        String nickListMsg = "";
        Enumeration<String> keys = onlineList.keys();
        while(keys.hasMoreElements()){
            nickListMsg = nickListMsg + "," + keys.nextElement();//�������б�����ס��֯���ַ���
        }

        nickListMsg = ClientMenu.RE_USERS_LIST + nickListMsg;

        keys = onlineList.keys();
        ChannelHandlerContext satTemp;
        while(keys.hasMoreElements()){
            satTemp = onlineList.get(keys.nextElement());
            satTemp.writeAndFlush(nickListMsg);// �����µ��б���Ϣ���͵������ͻ���
        }
    }

    public static void sendMessage(String message){
        String [] names = message.split("-");
        ChannelHandlerContext channel = onlineList.get(names[3]);
        channel.writeAndFlush(message.substring(0,message.lastIndexOf("-")) + "-" + names[2]);
    }

    /**
     * �����û������б�
     */
    public static void reUsersList() {
        serverView.textArea.setText(null);
        ChannelHandlerContext channel;

        Enumeration<String> keys = onlineList.keys();
        while(keys.hasMoreElements()){
            String nickName = keys.nextElement();
            channel = onlineList.get(nickName);
            String msg = "";
            msg += "Address:" + channel.channel().remoteAddress() + "\n"; //ƴ�ӵ�ַ
            msg += "name:" + nickName + "\n\n"; //ƴ���ǳ�
            serverView.textArea.append(msg);
        }

    }
}
