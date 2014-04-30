import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class ClientThread extends Thread {
	private CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	private CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
	private Selector selector = null;
	private SocketChannel socket = null;
	private SelectionKey clientKey = null;
	private String username;

	// 启动客户端
	public ClientThread(String username) {
		try {
			// 创建一个Selector
			selector = Selector.open();

			// 创建Socket并注册
			socket = SocketChannel.open();
			socket.configureBlocking(false);
			clientKey = socket.register(selector, SelectionKey.OP_CONNECT);

			// 连接到远程地址
			InetSocketAddress ip = new InetSocketAddress("localhost", 12345);
			socket.connect(ip);
			
			this.username = username;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 读取事件
	public void run() {
		try {
			// 监听事件
			while (true) {
				// 监听事件
				selector.select();
				// 事件来源列表
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					// 删除当前事件
					it.remove();

					// 判断事件类型
					if (key.isConnectable()) {
						// 连接事件
						SocketChannel channel = (SocketChannel) key.channel();
						if (channel.isConnectionPending())
							channel.finishConnect();
						channel.register(selector, SelectionKey.OP_READ);
						System.out.println("连接服务器端成功！");
						
						// 发送用户名
						send("username=" + this.username);
					} else if (key.isReadable()) {
						// 读取数据事件
						SocketChannel channel = (SocketChannel) key.channel();

						// 读取数据
						ByteBuffer buffer = ByteBuffer.allocate(50);
						channel.read(buffer);
						buffer.flip();
						String msg = decoder.decode(buffer).toString();
						System.out.println("收到：" + msg);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭
			try {
				selector.close();
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	// 发送消息
	public void send(String msg) {
		try {
			SocketChannel client = (SocketChannel) clientKey.channel();
			client.write(encoder.encode(CharBuffer.wrap(msg)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 关闭客户端
	public void close() {
		try {
			selector.close();
			socket.close();
		} catch (IOException e) {
		}
	}
}
