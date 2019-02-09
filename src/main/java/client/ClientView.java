package client;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import enu.ClientMenu;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

@SuppressWarnings("serial")
public class ClientView extends JFrame implements ActionListener{
	JLabel jlHost=new JLabel("������");//������ʾ�����������ı�ǩ
	JLabel jlPort=new JLabel("�˿ں�");////������ʾ����˿ںű�ǩ
	JLabel jlNickName=new JLabel("��    ��");//������ʾ�����ǳƵı�ǩ
	JTextField jtfHost=new JTextField("localhost");//�����������������ı���Ĭ��ֵ��"127.0.0.1"
	JTextField jtfPort=new JTextField("8090");//��������˿ںŵ��ı���Ĭ��ֵ��9999
	JTextField jtfNickName=new JTextField("Play1");//���������ǳƵ��ı���Ĭ��ֵ��Play1
	JButton jbConnection=new JButton("��  ��");//����"����"��ť
	JButton jbExtConnection=new JButton("��  ��");//����"�Ͽ�"��ť
	JButton jbAdmitDefeat=new JButton("��  ��");//����"����"��ť
	JButton jbChallenge=new JButton("��  ս");//����"��ս"��ť
	JComboBox jcbNickList=new JComboBox();//������ŵ�ǰ�û��������б��
	JTextArea textArea = new JTextArea();//���������	
	JScrollPane jscrollPane = new JScrollPane(textArea);
	JTextArea sendMessageText=new JTextArea();//����������Ϣ���ı���
	JButton jbSendMessage=new JButton("����");//����"����"��ť
	Checkerboard checkerboard = new Checkerboard(); //��������;
	Rule rule  = new Rule(checkerboard); //����������
	JPanel rightJPanel = new JPanel();//�����ұ߲˵���
	public String opponentName = null; //���ڶ��ĵĶ���
	public boolean isCanMove = true; //�Ƿ��ֵ�������ı��
	public boolean userCamp = true; //�ж��û���Ӫ
    public String myNickName;  //�ҵ��ǳ�
	public Channel channel; //netty�ṩ�Ľ���ͨ��
    public Thread myThread;
	private ClientView clientView ;
	int preX,preY;
	
	public ClientView(){
		initRightJPanl();//��ʼ���ұ߿�
		initButton();
		initCheckerboard();//��ʼ������
        clientView = this;
	}
	
	/**
	 * ��ʼ���ұ߿�
	 * @return
	 */
	public void initRightJPanl(){
		rightJPanel.setPreferredSize(new Dimension(200, 700));
		rightJPanel.setLayout(null);//��Ϊ�ղ���
		
		jlHost.setBounds(10,10,50,20);
		rightJPanel.add(jlHost);//���"������"��ǩ
		
		jtfHost.setBounds(100,10,80,20);
		rightJPanel.add(jtfHost);//��������������������ı���
		
		jlPort.setBounds(10,40,50,20);
		rightJPanel.add(jlPort);//���"�˿ں�"��ǩ
		
		jtfPort.setBounds(100,40,80,20);
		rightJPanel.add(jtfPort);//�����������˿ںŵ��ı���
		
		jlNickName.setBounds(10,70,50,20);
		rightJPanel.add(jlNickName);//���"�ǳ�"��ǩ
		
		jtfNickName.setBounds(100,70,80,20);
		rightJPanel.add(jtfNickName);//������������ǳƵ��ı���
		
		jbConnection.setBounds(10,100,80,20);
		rightJPanel.add(jbConnection);//���"����"��ť
		
		jbExtConnection.setBounds(110,100,80,20);
		rightJPanel.add(jbExtConnection);//���"�Ͽ�"��ť
		
		jcbNickList.setBounds(10,130,180,20);
		rightJPanel.add(jcbNickList);//���������ʾ��ǰ�û��������б��
		
		jbChallenge.setBounds(10,160,80,20);
		rightJPanel.add(jbChallenge);//���"��ս"��ť
		
		jbAdmitDefeat.setBounds(110,160,80,20);
		rightJPanel.add(jbAdmitDefeat);//���"����"��ť
		
		jscrollPane.setBounds(10, 190, 180, 400);
		textArea.setEditable(false);//����l����򲻿ɱ༭
		textArea.setBackground(new Color(200, 200, 200));//���ñ���ɫ
		jscrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); //���ù�������ֱ�Զ�����
		jscrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);//���ù�����ˮƽ�Զ�����
		rightJPanel.add(jscrollPane);//��������
		
		sendMessageText.setBounds(10, 600, 180, 50);
		rightJPanel.add(sendMessageText);//��ӷ��͵���Ϣ��
		
		jbSendMessage.setBounds(130, 660, 60, 20);
		rightJPanel.add(jbSendMessage);//��ӷ��Ͱ�ť
		rightJPanel.setBackground(new Color(128, 128, 128));
	}
	
	public void initButton(){
		//Ϊ��ťע�����
		jbConnection.addActionListener(this);
		jbExtConnection.addActionListener(this);
		jbChallenge.addActionListener(this);
		jbAdmitDefeat.addActionListener(this);
		jbSendMessage.addActionListener(this);
		
		//���ð�ť״̬Ϊ������
		jbExtConnection.setEnabled(false);
		jbChallenge.setEnabled(false);
		jbAdmitDefeat.setEnabled(false);
		jbSendMessage.setEnabled(false);
	}
	
	/**
	 * Ϊ������ťע���¼�
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == jbConnection) openConnection(); //��������
		if (e.getSource() == jbChallenge) launchChallenge();//������ս
		if (e.getSource() == jbExtConnection) extConnection();//�Ͽ�����
		if (e.getSource() == jbAdmitDefeat) admitDefeat(); //����
		if (e.getSource() == jbSendMessage) sendMessage(); //������Ϣ
	}
	
	public void openConnection() {
		int portText = 0;
		try {// ����û�����ĶϿںŲ�ת��Ϊ����
            portText = Integer.parseInt(jtfPort.getText().trim());
		} catch (Exception e) {// ��������������������ʾ
			JOptionPane.showMessageDialog(this, "�˿ں�ֻ��������", "����", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (portText > 65535 || portText < 0) {// �˿ںŲ��Ϸ�������������ʾ
			JOptionPane.showMessageDialog(this, "�˿ں�ֻ����0-65535������", "����", JOptionPane.ERROR_MESSAGE);
			return;
		}

        myNickName = jtfNickName.getText().trim();// ����ǳ�
		if (myNickName.length() == 0) {// �ǳ�Ϊ�գ�����������ʾ��Ϣ
			JOptionPane.showMessageDialog(this, "�����������Ϊ��", "����", JOptionPane.ERROR_MESSAGE);
			return;
		}
        final int port = portText;

        myThread = new Thread(() ->{
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                        .handler(new ClientInitializer());
                ChannelFuture channelFuture = bootstrap.connect("localhost",port).sync();
                channel = channelFuture.channel();
                channel.writeAndFlush(ClientMenu.REQUEST_CONNECTION + myNickName);//�����ǳƵ�������

                jtfHost.setEnabled(false);// �������������������ı�����Ϊ������
                jtfPort.setEnabled(false);// ����������˿ںŵ��ı�����Ϊ������
                jtfNickName.setEnabled(false);// �����������ǳƵ��ı�����Ϊ������
                jbConnection.setEnabled(false);// ��"����"��ť��Ϊ������
                jbExtConnection.setEnabled(true);// ��"�Ͽ�"��ť��Ϊ����
                jbChallenge.setEnabled(true);// ��"��ս"��ť��Ϊ����
                JOptionPane.showMessageDialog(this, "�����ӵ�������", "��ʾ", JOptionPane.INFORMATION_MESSAGE);// ���ӳɹ���������ʾ��Ϣ

                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(clientView, "���ӷ�����ʧ��", "����", JOptionPane.ERROR_MESSAGE);// ����ʧ�ܣ�������ʾ��Ϣ
            }finally {
                workerGroup.shutdownGracefully();
            }
        });

        myThread.start();
	}
	
	/**
	 * ������ս
	 */
	public void launchChallenge() {
        String name = (String)jcbNickList.getSelectedItem();

        if (name == null ||  name.equals("")) {
            JOptionPane.showMessageDialog(this, "��ѡ��Է�����", "����", JOptionPane.ERROR_MESSAGE);
            return;
        }

        channel.writeAndFlush(ClientMenu.LAUNCH_CHALLENGE  + myNickName + "-" +  name);	// �������������ս��Ϣ
        JOptionPane.showMessageDialog(this, "�������ս,��ȴ��ָ�...", "��ʾ", JOptionPane.INFORMATION_MESSAGE);
	}
	
	
	//�Ͽ�����
	public void extConnection(){

		if(channel != null){
            if(opponentName != null){//��������ڶ��ĵĶ�������϶��ֵ����ַ���������ط���Ϣ
                channel.writeAndFlush(ClientMenu.USER_EXIT + myNickName + "-" + opponentName);//����������ͶϿ����ӵ�����
            }else{
                channel.writeAndFlush(ClientMenu.USER_EXIT + myNickName);//����������ͶϿ����ӵ�����
            }
            channel.close();
        }

        userCamp = true;//��ʼ����Ӫ
        opponentName = null;//�����ÿ�
        isCanMove = true;//��ʼ��������
        textArea.setText(null);//��ʼ�������
        //��ʼ�����ְ�ť
        jtfHost.setEnabled(true);
        jtfPort.setEnabled(true);
        jtfNickName.setEnabled(true);
        jbConnection.setEnabled(true);
        jbExtConnection.setEnabled(false);
        jbChallenge.setEnabled(false);
        jbAdmitDefeat.setEnabled(false);
        jbSendMessage.setEnabled(false);
        jcbNickList.removeAllItems();
	}
	
	// ����
	public void admitDefeat() {
        if (JOptionPane.showConfirmDialog(this, "��ȷ��Ҫ������", "���䣡",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            channel.writeAndFlush(ClientMenu.ADMIT_DEFEAT + opponentName);// ��������������������
            jbChallenge.setEnabled(true);// ��"��ս"��ť�����
            jbAdmitDefeat.setEnabled(false);// ��"����"��ť��Ϊ������
            opponentName = null;//�����ÿ�
            isCanMove = true;//��ʼ��������
            //��ʼ������
            if(clientView.userCamp){
                clientView.checkerboard.initPiece(false);
            }else{
                clientView.checkerboard.initPiece(true);
            }
            jbSendMessage.setEnabled(false);//�����Ͱ�ť������
            textArea.setText(null);//��ʼ�������
        }
	}
	
	/**
	 * ������Ϣ
	 */
	public void sendMessage() {
        String message = sendMessageText.getText().trim();
        if (opponentName == null)
            return; // ���û�����ڶ�ս�Ķ���ֱ�ӷ���

        if (message != null && message.length() > 0) {

            channel.writeAndFlush(ClientMenu.CHAT + message + "-" + myNickName + "-" + opponentName);// �������������Ϣ
            textArea.append("���"+opponentName+"˵:" + "\n    " + message + "\n\n");
            sendMessageText.setText(null);

        }
	}
	
	private void initCheckerboard() {
		
		//���������¼�
		checkerboard.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				int x = e.getX() / Checkerboard.DIAMETER;
				int y = e.getY() / Checkerboard.DIAMETER;

				boolean isMove = false;

				Piece piece = isStep();
				if (piece != null) {// �ж��ҷ���Ӫ��û�����ӱ�ѡ��
					if(isCanMove){ //�ж��Ƿ��ֵ�������
						if (rule.getRule(piece, x, y)) {// �ж��Ƿ��������
							
							if (checkerboard.pieceList[x][y] == null
									|| checkerboard.pieceList[x][y].getCamp() != piece.getCamp()) {
								
								Piece target = checkerboard.pieceList[x][y];
								int startX = piece.getX();
								int startY = piece.getY();
								pieceMove(piece , x ,y); //����
								checkerboard.repaint();//�ػ�
								
								if (opponentName == null) {
									if (userCamp) {
										userCamp = false;
									} else {
										userCamp = true;
									}
								} else {
									//�����������Ϣ
                                    channel.writeAndFlush(
                                            ClientMenu.MOVE + opponentName + "," + startX + "," + startY + "," + x + "," + y);
                                    isCanMove = false;
								}
								
								if (target != null)// �ж��Ƿ�ֳ�ʤ��
									isWinning(target, x, y);
								
								isMove = true;
								
							}
						}
					}
					
				}

				// �������ӵ�ѡ��״̬
				if (!isMove) {// �ж��Ƿ�����
					if (isEmtry(x, y) != null) { // �жϵ���ĵط��Ƿ�Ϊ��
						if (checkerboard.pieceList[x][y].getCamp() == userCamp) {// �жϸ�������Ӫ������Ƿ���ͬ
							if (checkerboard.pieceList[x][y].getSelection()) {
								checkerboard.pieceList[x][y].setSelection(false);
							} else {
								checkerboard.pieceList[x][y].setSelection(true);
							}

							if (preX != x || preY != y) {
								if (checkerboard.pieceList[preX][preY] != null)
									checkerboard.pieceList[preX][preY].setSelection(false);
							}

							preX = x;
							preY = y;
						}
					}
				}

				checkerboard.repaint();
			}
		});
		
		//���һ���ر��¼�
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				String str = "�Ƿ�Ҫ�˳���Ϸ?";
				// �����Ϣ�Ի���
				if (JOptionPane.showConfirmDialog(clientView, str, "�˳���Ϸ",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					extConnection(); //֪ͨ�������Ͽ�����
					System.exit(0); // �˳�
				}
			}
		});
		
		
		JSplitPane jsp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,checkerboard,rightJPanel);//����һ��JSplitPane
		jsp.setEnabled(false); //���÷ָ�����ֹ�϶�
		add(jsp);
		setTitle("�й�����");
		setResizable(false);// ���ô˴����Ƿ�����û�������С
		setVisible(true); //���ô�����ʾ״̬
		pack();//���ݴ�������Ĳ��ּ������preferredSize��ȷ��frame����Ѵ�С��
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	/**
	 * �ж��Ƿ�ֳ�ʤ��
	 * @return
	 */
	public void isWinning(Piece target ,int x, int y ){
		
		if (target.getName() == "��" || target.getName() == "��") {
			String resultMessage;
			if (checkerboard.pieceList[x][y].getCamp()) {
				resultMessage = "�췽ʤ��";
			} else {
				resultMessage = "�ڷ�ʤ��";
			}
			JOptionPane.showMessageDialog(this, resultMessage, "��Ϸ����",
					JOptionPane.PLAIN_MESSAGE); // ������Ϣ��

			if(opponentName != null && userCamp){
				checkerboard.initPiece(false);
				isCanMove = true;//��ʼ��������
			}else if(opponentName != null && userCamp == false){
				checkerboard.initPiece(true);
				isCanMove = false;//��ʼ��������
			}else{
				checkerboard.initPiece(false);
			}
		}
		
	}
	
	/**
	 * ����
	 * @return
	 */
	public void pieceMove(Piece piece,int x,int y){
		
		checkerboard.pieceList[x][y] = piece;
		checkerboard.pieceList[piece.getX()][piece.getY()] = null;
		checkerboard.pieceList[x][y].setX(x);
		checkerboard.pieceList[x][y].setY(y);
		checkerboard.pieceList[x][y].setSelection(false);
	}
	

	//�жϺ������ͬ��Ӫ��������û�б�ѡ�е�
	public Piece isStep(){
		Piece item;
		for (int i = 0; i < checkerboard.pieceList.length; i++) {
			
			for (int j = 0; j < checkerboard.pieceList[i].length; j++) {
				item = checkerboard.pieceList[i][j];
				if(item != null){
					if(item.getSelection() && item.getCamp() == userCamp) return item;
				}
			}
		}
		return null;
	}
	
	
	//�ж�������ĵط���û������
	public Piece isEmtry(int x , int y){
		Piece item;
		for (int i = 0; i < checkerboard.pieceList.length; i++) {
			for (int j = 0; j < checkerboard.pieceList[i].length; j++) {
				item = checkerboard.pieceList[i][j];
				if(checkerboard.pieceList[i][j] != null){
					if(item.getX() == x && item.getY() == y) return item;
				}
			}
		}
		return null;
	}

    private class ClientInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline cp = ch.pipeline();
            cp.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));

            cp.addLast(new LengthFieldPrepender(4));
            cp.addLast(new StringDecoder(CharsetUtil.UTF_8));
            cp.addLast(new StringEncoder(CharsetUtil.UTF_8));
            cp.addLast(new ClientHandler(clientView));
        }
    }
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ClientView view = new ClientView();
	}

	
}
