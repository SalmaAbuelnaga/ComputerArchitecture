package CA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class harvard {
	static short[] insMem = new short[1024];
	static byte[] dataMem = new byte[2048];
	static byte[] registers = new byte[65];
	int noOfCycles;
	int[] fetch = new int[noOfCycles];
	int[] decode = new int[noOfCycles];
	int[] execute = new int[noOfCycles];
	static short instruction = 0;
	static byte op1 = 0;
	static byte op2 = 0;
	int index = -1;// index we will update wether data memory or reg
	static byte operator = 0;
	static int op1regindex = -1;// 6bits
	static int op2regindex = -1;
	short newpc;
	int updateddataMemindx = -1;
	static boolean pcUpdated = false;
	static boolean registerUpdated = false;
	static boolean statusUpdated = false;
	static boolean dataMemUpdated = false;
	static int cf = 0;
	static int of = 0;
	static short pc;
	static short pctemp;
	static boolean branch = false;

	public static void updates() {
		if (pcUpdated == true) {
			System.out.println("pc=" + pc);
		}

		if (registerUpdated == true) {

			System.out.println("r" + op1regindex + "<-----  " + registers[op1regindex]);
		}
		if (dataMemUpdated == true) {
			// dataMemUpdated=index;
			System.out.println("m[" + op2 + "]" + "=" + dataMem[op2]);
		}
		if (statusUpdated) {
			String s = Integer.toBinaryString(registers[64]);
			while (s.length() < 8) {
				s = "0" + s;
			}
			System.out.println("status reg" + "=" + s);
		}
	}

	public static void fetch() {
		instruction = insMem[pc];
		pc++;
	}

	public static void decode() {

		String op = "";
		String r1 = "";
		String rtt = "";

		String st = Integer.toBinaryString(instruction);
		// returns 32bits
		if (st.length() < 16) {
			//System.out.println(16 - st.length());
			while (st.length() < 16) {
				st = "0" + st;
			}
		}
		if(st.length()>16) {
			st = st.substring(st.length()-16);
		}
		// System.out.println(st); System.out.println(st.length());
		int k = 0;
		for (int i = st.length() - 1; i >= 0; i--) {

			if (i >= 12) {// System.out.println("yy");
				op = op + st.charAt(k);
			}
			// System.out.println(op);
			if (i <= 11 && i >= 6) {
				r1 = r1 + st.charAt(k);
			}
			//
			if (i >= 0 && i <= 5) {
				rtt = rtt + st.charAt(k);
			}

			k++;

		}
		// System.out.print(op);
		// int opcode=Integer.parseInt(op,2);
		byte opcode = Byte.parseByte(op, 2);
		int rs = Integer.parseInt(r1, 2);
		int rt = Integer.parseInt(rtt, 2); // this maybe signed
		// byte rsr = registers[rs];
		op1 = registers[rs];
		op1regindex = rs;
		operator = opcode;
		if (opcode == 0 || opcode == 1 || opcode == 2 ||opcode ==5|| opcode == 6 || opcode == 7) {
			op2 = registers[rt];
			op2regindex = rt;
		} else {// opcode immediate
			if (opcode == 4 || opcode == 7) {// sign extend of immediate values from 6 to 8 bits
				pctemp = (short) (pc - 1);
			}
			// first bit is the sign bit always
			if (opcode < 10) {// opcodes 10 and 11 are not signed, any other immediate value is signed
				char c = rtt.charAt(0);// akher bit

				for (int i = 0; i < 2; i++) {//sign extend
					rtt = c + rtt;
				}
				if (c=='1') {
					rtt = rtt.replace("0", " "); 
					rtt = rtt.replace("1", "0"); 
					rtt = rtt.replace(" ", "1"); 
					int o = Integer.parseInt(rtt, 2);
					o= (o+1)*-1;
					op2 = (byte)o;
			}
				else
					op2 = Byte.parseByte(rtt, 2);
			}
			else
				op2 = Byte.parseByte(rtt, 2);
		}
	}


	public static void setZeroFlag(byte rs) {
		byte b = registers[64];
		if (rs == 0) {
			b = (byte) (b | (1 << 0));
			registers[64] = b;
		} else {
			b = (byte) (b & ~(1 << 0));
			registers[64] = b;
		}
	}

	public static void setNegFlag(byte rs) {
		byte b = registers[64];
		if (rs < 0) {
			b = (byte) (b | (1 << 2));
			registers[64] = b;
		} else {
			b = (byte) (b & ~(1 << 2));
			registers[64] = b;
		}
	}

	public static void setSignFlag() {
		byte b = registers[64];
		byte neg = (byte) ((b >> 2) & 1);
		byte ov = (byte) ((b >> 3) & 1);
		byte r = (byte) (neg ^ ov);
		b = (byte) (b | (1 << 1));
		if (r == 1) {
			b = (byte) (b | (1 << 1));
			registers[64] = b;
		} else {
			b = (byte) (b & ~(1 << 1));
			registers[64] = b;
		}

	}

	public static byte src() {
		int c = (op1 >>> op2) | (op1 << (8 - op2));
		return (byte) c;
	}

	public static byte slc() {
		int c = ((op1 << op2) | (op1 >>> (8 - op2)));
		return (byte) c;
	}

	public static void execute() {
		registers[64]=0;
		registerUpdated = false;
		dataMemUpdated = false;
		pcUpdated = false;
		statusUpdated = false;

		switch (operator) {
		case 0:
			registerUpdated = true;
			op1 = (byte) add();
			registers[op1regindex] = op1;
			setZeroFlag(op1);

			setNegFlag(op1);
			setOverFlag();
			setCarryFlag();
			setSignFlag();
			statusUpdated = true;
			System.out.println("r" + op1regindex + "<-----------  " + "r" + op1regindex + "+" + "r" + op2regindex);
			break;
		case 1:
			registerUpdated = true;
			op1 = sub();
			registers[op1regindex] = op1;

			setZeroFlag(op1);
			setOverFlag();
			setCarryFlag();
			setNegFlag(op1);
			setSignFlag();
			statusUpdated = true;
			System.out.println("r" + op1regindex + "<-----------  " + "r" + op1regindex + "-" + "r" + op2regindex);
			break;
		case 2:
			registerUpdated = true;
			op1 = Mul();
			registers[op1regindex] = op1;
			setZeroFlag(op1);
			setCarryFlag();
			setNegFlag(op1);
			statusUpdated = true;
			System.out.println("r" + op1regindex + "<-----------  " + "r" + op1regindex + "*" + "r" + op2regindex);
			break;
		case 3:
			registerUpdated = true;
			registers[op1regindex] = op2;
			System.out.println("r" + op1regindex + "<-----------  " + op2);
			break;
		case 4:
			if (op1 == 0) {
				branch = true;
				pctemp = (short) (pctemp + op2 + 1);
				pcUpdated = true;
				System.out.println("BEQZ" + "r" + op1regindex + " " + op2);
			}
			break;
		case 5:
			op1 = and();
			registers[op1regindex] = op1;
			setZeroFlag(op1);
			setNegFlag(op1);
			statusUpdated = true;
			System.out.println("r" + op1regindex + "<-----------  " + "r" + op1regindex + "AND" + "r" + op2regindex);
			break;
		case 6:
			registerUpdated = true;
			op1 = or();
			registers[op1regindex] = op1;
			setZeroFlag(op1);
			setNegFlag(op1);
			statusUpdated = true;
			System.out.println("r" + op1regindex + "<-----------  " + "r" + op1regindex + "OR" + "r" + op2regindex);
			break;
		case 7:
			pcUpdated = true;
			pctemp = jr();
			branch = true;
			break;
		case 8:
			registerUpdated = true;
			op1 = slc();
			registers[op1regindex] = op1;
			setZeroFlag(op1);
			setNegFlag(op1);
			statusUpdated = true;
			System.out.println("r" + op1regindex + "<---------  " + "slc" + " r" + op1regindex + " " + op2);
			break;
		case 9:
			registerUpdated = true;
			op1 = src();
			registers[op1regindex] = op1;
			setZeroFlag(op1);
			setNegFlag(op1);
			statusUpdated = true;
			System.out.println("r" + op1regindex + "<---------  " + "src" + " r" + op1regindex + " " + op2);
			break;
		case 10:
			registerUpdated = true;
			registers[op1regindex] = (byte) dataMem[op2];
			System.out.println(" r" + op1regindex + "<------------ " + "Mem[" + op2 + "]");
			break;
		case 11:
			dataMemUpdated = true;
			dataMem[op2] = op1;
			System.out.println("Mem[" + op2 + "]" + " r" + op1regindex);
			break;

		default:
			break;

		}
	}

	private static Short jr() {
		String o1 = Integer.toBinaryString(op1);
		String o2 = Integer.toBinaryString(op2);
		if (op1 < 0) {
			o1 = o1.substring(26);
		}
		if (op2 < 0) {
			o2 = o2.substring(26);
		}
		if (o1.length() < 8) {
			for (int i = 0; i < 8 - o1.length(); i++) {
				o1 = "0" + o1;
			}

		}

		if (o2.length() < 8) {
			for (int i = 0; i < 8 - o2.length(); i++) {
				o2 = "0" + o2;
			}

		}
		o1 = o1 + o2;
		return Short.parseShort(o1, 2);

	}

	public static byte add() {
		int ans2 = op1 + op2;
		byte ans1 = (byte) (op1 + op2);
		cf = setcf(ans1, ans2);
		of = setof(ans1, ans2);
		return ans1;
	}

	public static byte sub() {
		int ans2 = op1 - op2;
		byte ans1 = (byte) (op1 - op2);
		cf = setcf(ans1, ans2);
		of = setof(ans1, ans2);
		return ans1;
	}

	public static int setof(byte ans1, int ans2) {
		if (ans1 > 0 && ans2 < 0) {
			return 1;
		}
		if (ans2 > 0 && ans1 < 0) {
			return 1;
		}
		return 0;
	}

	public static int setcf(byte ans1, int ans2) {
		String temp2 = Integer.toBinaryString(ans2);
		if(ans2<0) {
			return Integer.parseInt(temp2.charAt(temp2.length()-8)+"");
			 
		}
		else {
			return 0;
		}

	}

	public short[] init(String filename) {
		short[] inst = parse(readFile(filename));
		int instCount = inst.length;
		noOfCycles = 3 + (instCount - 1) * 1;
		return inst;

	}

	public short[] parse(ArrayList<String[]> a) {
		short[] inst = new short[a.size()];
		String opcode = "";
		for (int i = 0; i < a.size(); i++) {
			String[] s = a.get(i);
			switch (s[0]) {
			case "ADD":
				opcode = "0000";
				break;
			case "SUB":
				opcode = "0001";
				break;
			case "MUL":
				opcode = "0010";
				break;
			case "LDI":
				opcode = "0011";
				break;
			case "BEQZ":
				opcode = "0100";
				break;
			case "AND":
				opcode = "0101";
				break;
			case "OR":
				opcode = "0110";
				break;
			case "JR":
				opcode = "0111";
				break;
			case "SLC":
				opcode = "1000";
				break;
			case "SRC":
				opcode = "1001";
				break;
			case "LB":
				opcode = "1010";
				break;
			case "SB":
				opcode = "1011";
				break;
			}
			String c = s[1].substring(1);// 0,1,2,3...
			int r = Integer.parseInt(c);
			String r1 = getBinReg(r);
			String r2 = "";
			if (isInteger(s[2])) {
				r2 = getBin(Integer.parseInt(s[2]));
			} else {// second op is a register
				c = s[2].substring(1);// 0,1,2,3...
				r = Integer.parseInt(c);
				r2 = getBinReg(r);
			}
			String in = opcode + r1 + r2;// binary string of instruction
			// System.out.println("op"+in);
			short rt = (short) Integer.parseInt(in, 2);
			inst[i] = rt;

		}
		return inst;
	}

	public static String getBin(int r) {
		String r1 = Integer.toBinaryString(r);// does sign extend//should be 5 bits or less
		if (r1.length() > 6) {
			r1 = r1.substring(26);
		} else {
			r1 = getBinReg(r);
		}
		return r1;
	}

	public static String getBinReg(int r) {// value +ve will always return less than 6bits or exact
		String r1 = Integer.toBinaryString(r);
		if (r1.length() < 6) {
			while (r1.length() < 6) {
				r1 = '0' + r1;
			}

		}
		return r1;
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}

	public static ArrayList<String[]> readFile(String filename) {
		String currentLine = "";
		ArrayList<String[]> line = new ArrayList<String[]>();
		FileReader fileReader;
		try {
			fileReader = new FileReader(filename);
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fileReader);
			while ((currentLine = br.readLine()) != null) {
				String[] line1 = currentLine.split("\\s+");
				line.add(line1);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("line:"+Arrays.toString(line.get(8)));
		return line;
	}

	public static byte Mul() {
		byte R1 = 0;
		R1 = (byte) (op1 * op2);
		int R2 = op1 * op2;
		cf = setcf(R1, R2);
		of = setof(R1, R2);
		return R1;

	}

	private static byte or() {
		return (byte) (op1 | op2);

	}

	private static byte and() {
		return (byte) (op1 & op2);

	}

	public void exec1(String f) {
		for (int i = 0; i < registers.length; i++) {
			registers[i] = 0;
		}

		insMem = init(f);
		int n = noOfCycles;// get number of ins from number of lines
		// parse instructions
		data();
		System.out.println("");
		int d = -1;
		int e = -2;
		int ft = 0;
		int s = insMem.length;
		int k = 1;
		boolean one = true;
		boolean ond = true;
		for (int i = 1; i <= n; i++) {
			if (branch) {
				branch = false;
				pc = pctemp;
				i = pc + 1;
				ft = pc;
				d = ft - 1;
				e = d - 1;
				one = false;
				ond = false;

			}
			System.out.println("cycle" + k);
			if (e >= 0 && e <= s && one != false) {
				System.out.println("executing ins" + e);
				execute();

				updates();
			}
			if (d >= 0 && d < s && ond != false) {
				decode();
				System.out.println("decoding ins" + d);
				one = true;
			}
			if (ft >= 0 && ft < s) {
				fetch();
				System.out.println("fetching ins" + ft);
			}
			System.out.println("-----------------");
			d++;
			e++;
			ft++;
			k++;
			ond = true;
		}
		data();
	}

	public static void setOverFlag() {
		byte b = registers[64];
		if (of == 0) {
			b = (byte) (b & ~(1 << 3));
			registers[64] = b;
		} else {
			b = (byte) (b | (1 << 3));
			registers[64] = b;
		}

	}

	public void data() {
		for (int i = 0; i < registers.length; i++) {
			System.out.print("r" + i + "=" + registers[i] + "  ");
		}
	}

	public static void setCarryFlag() {
		byte b = registers[64];
		if (cf == 1) {
			b = (byte) (b | (1 << 4));
			registers[64] = b;
		} else {
			b = (byte) (b & ~(1 << 4));
			registers[64] = b;
		}
	}

	public static void main(String[] args) {
		harvard h = new harvard();
		h.exec1("src/Program 2.txt");

	}
}
