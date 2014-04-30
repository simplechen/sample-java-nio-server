import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		// 客户端列表
		Hashtable<String, SocketChannel> clientlist = new Hashtable<String, SocketChannel>();

		Selector selector = null;
		ServerSocketChannel server = null;
		try {
			// 创建一个Selector
			selector = Selector.open();

			// 创建Socket并注册
			server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.register(selector, SelectionKey.OP_ACCEPT);

			// 启动端口监听
			InetSocketAddress ip = new InetSocketAddress(1001);
			server.socket().bind(ip) ;


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
					if (key.isAcceptable()) {
						// 连接事件
						ServerSocketChannel server2 = (ServerSocketChannel) key.channel();
						SocketChannel channel = server2.accept();
						channel.configureBlocking(false);
						channel.register(selector, SelectionKey.OP_READ);
						System.out.println("客户端连接："+ channel.socket().getInetAddress().getHostName() + ":"+ channel.socket().getPort());
						
					} else if (key.isReadable()) {
						// 读取数据事件
						SocketChannel channel = (SocketChannel) key.channel();

						// 读取数据
						CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
								
						ByteBuffer buffer = ByteBuffer.allocate(50);
						channel.read(buffer);
						buffer.flip();
						String msg = decoder.decode(buffer).toString();
						System.out.println("收到：" + msg);

						if (msg.startsWith("username=")) {
							String username = msg.replaceAll("username=", "");
							clientlist.put(username, channel);
						} else {
							// 转发消息给客户端
							String[] arr = msg.split(":");
							if (arr.length == 3) {
								String from = arr[0];//发送者
								String to = arr[1];//接收者
								String content = arr[2];//发送内容

								if (clientlist.containsKey(to)) {
									CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
									// 给接收者发送消息
									clientlist.get(to).write(encoder.encode(CharBuffer.wrap(from+ ":" + content)));
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭
			try {
				selector.close();
				server.close();
			} catch (IOException e) {
			}
		}
	}
}
