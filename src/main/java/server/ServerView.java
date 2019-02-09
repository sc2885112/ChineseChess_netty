package server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ServerView extends JFrame implements ActionListener{
	//����һ���ı���
	JLabel jlPort=new JLabel("�� �� ��");//������ʾ����˿ںű�ǩ
	JTextField jtfPort=new JTextField("8090");//��������˿ںŵ��ı���
	JButton jbStart=new JButton("����");//����"����"��ť
	JButton jbStop=new JButton("�ر�");//����"�ر�"��ť
	JTextArea textArea = new JTextArea();//��������ı���
	JScrollPane jscrollPane = new JScrollPane(textArea);//����������
	JPanel jps=new JPanel();//����һ��JPanel����


	ServerView serverView;
	public ServerView(){
		//��ʼ���ұ߿�
		initRightJPanel();
		
		//��ʼ������
		initUi();
		
		serverView = this;
	}
	
	
	public void initUi(){
		
		jscrollPane.setPreferredSize(new Dimension(200, 300));
		textArea.setEditable(false);
		
		jscrollPane.setHorizontalScrollBarPolicy(
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); //���ù�������ֱ�Զ�����
		jscrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);//���ù�����ˮƽ�Զ�����
		
		JSplitPane jsp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,jscrollPane,jps);//����һ��JSplitPane
		this.add(jsp);
		this.setTitle("������");
		this.setResizable(false);// ���ô˴����Ƿ� �����û�������С
		this.setVisible(true); //���ô�����ʾ״̬
		this.pack();//���ݴ�������Ĳ��ּ������preferredSize��ȷ��frame����Ѵ�С��
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	public void initRightJPanel(){
		jps.setPreferredSize(new Dimension(200, 300));
		jps.setLayout(null);//��Ϊ�ղ���
		
		//���ÿؼ�����ʹ�С
		jlPort.setBounds(20,20,50,20);
		jtfPort.setBounds(85,20,60,20);
		jbStop.setBounds(85,50,60,20);
		jbStart.setBounds(18,50,60,20);
		
		jps.add(jlPort);
		jps.add(jtfPort);
		jps.add(jbStop);
		jps.add(jbStart);
		
		//����ťע���¼�����
		jbStop.addActionListener(this);
		jbStart.addActionListener(this);
		//�رհ�ť��ʼ����
		jbStop.setEnabled(false);
	}
	
	/**
	 * Ϊ�����͹ر�ע���¼�
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == jbStart) StartEvent();// ������"����"��ťʱ
		
		if (e.getSource() == jbStop) {// ����"�ر�"��ť��
			
		}

	}
	
	/**
	 * ������ť���¼�
	 */
	public void StartEvent(){
		int portText = 0;

		try {
            portText=Integer.parseInt(this.jtfPort.getText().trim());//����û�����Ķ˿ںţ���ת��Ϊ����
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,"�˿ں�ֻ��������","����",JOptionPane.ERROR_MESSAGE);
			return;
		}
	
		if (portText > 65535 || portText < 0) {// �ϿںŲ��Ϸ���������ʾ��Ϣ
			JOptionPane.showMessageDialog(this, "�˿ں�ֻ����0-65535������", "��Ϣ", JOptionPane.ERROR_MESSAGE);
			return;
		}

        /**
         * ����netty�����鲢����8090�˿�
         */

        final int port = portText;
		try {

		    new Thread(() ->{
                EventLoopGroup boosGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try{
                    ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap.group(boosGroup,workerGroup).channel(NioServerSocketChannel.class).childHandler(new ServerInitializer());
                    ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
                    ServerInteractiveUitl.setServerView(serverView);
                    channelFuture.channel().closeFuture().sync();
                }catch (Exception e){
		            e.printStackTrace();
                } finally{
                    boosGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }).start();

            //���������ɹ�����Ϣ
            JOptionPane.showMessageDialog(this,"�����ɹ�","��Ϣ",JOptionPane.INFORMATION_MESSAGE);
            jbStart.setEnabled(false);//����������ť
            jbStop.setEnabled(true);//�����رհ�ť
		} catch (Exception e) {
			System.err.println("���������ťʱ���ִ���");
			e.printStackTrace();
		}

    }
	

    private class ServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline cp = ch.pipeline();

            cp.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
            cp.addLast(new LengthFieldPrepender(4));
            cp.addLast(new StringDecoder(CharsetUtil.UTF_8));
            cp.addLast(new StringEncoder(CharsetUtil.UTF_8));
            cp.addLast(new ServerHandler());

        }
    }
	
	public static void main(String[] args) {
		ServerView sv = new ServerView();
	}


}
