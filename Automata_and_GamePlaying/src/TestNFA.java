import java.util.ArrayList;


public class TestNFA {
	static int maxn = 200; // maximum number of states
	static int symbol = 2; // number of symbols ('0','1')
	static int epssymbol = 2;

	// g[s1][i][s2]=true if and only if there's an edge with symbol i from state
	// s1 to s2
	static boolean[][][] g = new boolean[maxn][symbol + 1][maxn];
	static boolean[][][] g2 = new boolean[maxn][symbol][maxn];

	// closure[s1][s2] is true if and only if s2 is in CL(s1)
	static boolean[][] closure = new boolean[maxn][maxn];

	// next[i]=i if the regular expression at position i is not '('
	// next[i]=j if the regular expression at position i is '(' and jth position
	// holds the corresponding ')'
	static int[] next;

	static int state = 0; // current number of states

	public static ArrayList epsnfa_to_re(int enfa[]) {
		// copy g to g2
		for (int i = 0; i < state; i++) {
			for (int j = 0; j < symbol; j++) {
				for (int k = 0; k < state; k++) {
					g2[i][j][k] = g[i][j][k];
				}
			}
		}
		// closure part
		for (int i = 0; i < state; i++) {
			for (int j = 0; j < state; j++) {
				if (closure[i][j] == true) {
					for (int k = 0; k < state; k++) {
						for (int sym = 0; sym < symbol; sym++) {
							if (g[j][sym][k] == true && g2[i][sym][k] == false)
								g2[i][sym][k] = true;
						}
					}
				}
			}
		}
		
		ArrayList<Integer> nfa = new ArrayList<Integer>();
		nfa.add(enfa[0]);
		nfa.add(enfa[1]);
		for (int i = 0; i < state; i++) {
			if (closure[i][enfa[1]] == true)
				if (!nfa.contains(i))
					nfa.add(i);
		}
		System.out.println(nfa.toString());
		for (int i = 0; i < state; i++) {
			System.out.print(i+": ");
			for (int j = 0; j < state; j++)
				if (g2[i][0][j] == true)
				System.out.print(j + " ");
			System.out.println();
		}
		for (int i = 0; i < state; i++) {
			System.out.print(i+": ");
			for (int j = 0; j < state; j++)
				if (g2[i][1][j] == true)
				System.out.print(j + " ");
			System.out.println();
		}
		return nfa;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		state = 6;
		for (int i = 0; i < state; i++) {
			closure[i][i] = true;
			for (int j = 0; j < state; j++) {
				for (int sym = 0; sym < symbol; sym++)
					g[i][sym][j] = false;
			}
		}
		g[0][0][4] = true;
		g[0][1][1] = true;
		g[1][1][2] = true;
		closure[1][3] = true;
		g[2][1][3] = true;
		g[4][0][5] = true;
		closure[4][1] = closure[4][2] = closure[4][3] = true;
		g[5][0][3] = true;
		
		int enfa[] = new int[2];
		enfa[0] = 0;
		enfa[1] = 3;
		epsnfa_to_re(enfa);
	}

}
