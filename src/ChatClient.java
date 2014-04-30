import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChatClient {

	public static void main(String[] args) {
		String username = args[0];
		ClientThread client = new ClientThread(username);
		client.start();

		// 输入输出流
		BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

		try {
			// 循环读取键盘输入
			String readline;
			while ((readline = sin.readLine()) != null) {
				if (readline.equals("bye")) {
					client.close();
					System.exit(0);
				}
				client.send(username + ":" + readline);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
