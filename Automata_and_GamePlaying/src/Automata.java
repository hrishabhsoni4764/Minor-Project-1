/* 
 * Converting regular expression into Epsilon-NFA
 * Author: Chenguang Zhu
 * CS154, Stanford University
 */

import java.io.*;
import java.util.*;

public class Automata {
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

	// add edge from s1 to s2 with symbol c
	static void addEdge(int s1, int c, int s2) {
		g[s1][c][s2] = true;
	}

	// increase the number of states of NFA by 1
	static int incCapacity() {
		int i, j;
		for (i = 0; i <= state; ++i)
			for (j = 0; j <= symbol; ++j)
				g[i][j][state] = g[state][j][i] = false;
		return (state++);
	}

	// unite two Epsilon-NFAs, with start state s1 and s2, final state t1 and
	// t2, respectively
	// return an array of length 2, where the first element is the start state
	// of the combined NFA. the second being the final state
	static int[] union(int s1, int t1, int s2, int t2) {
		int[] st = new int[2];

		// Please fill in the program here
		st[0] = incCapacity();
		st[1] = incCapacity();
		addEdge(st[0], epssymbol, s1);
		addEdge(st[0], epssymbol, s2);
		addEdge(t1, epssymbol, st[1]);
		addEdge(t2, epssymbol, st[1]);

		return st;
	}

	// concatenation of two Epsilon-NFAs, with start state s1 and s2, final
	// state t1 and t2, respectively
	// return an array of length 2, where the first element is the start state
	// of the combined NFA. the second being the final state
	static int[] concat(int s1, int t1, int s2, int t2) {
		int[] st = new int[2];
		// Please fill in the program here
		st[0] = s1;
		st[1] = t2;
		addEdge(t1, epssymbol, s2);
		return st;
	}

	// Closure of a Epsilon-NFA, with start state s and final state t
	// return an array of length 2, where the first element is the start state
	// of the closure Epsilon-NFA. the second being the final state
	static int[] clo(int s, int t) {
		int[] st = new int[2];
		// Please fill in the program here
		st[0] = incCapacity();
		st[1] = incCapacity();
		addEdge(st[0], epssymbol, s);
		addEdge(t, epssymbol, st[1]);
		addEdge(t, epssymbol, s);
		addEdge(st[0], epssymbol, st[1]);
		return st;
	}

	// Calculate the closure: CL()
	static void calc_closure() {
		int[] queue = new int[maxn];
		int head, tail, i, j, k;
		for (i = 0; i < state; ++i) {
			for (j = 0; j < state; ++j)
				closure[i][j] = false;

			// Breadth First Search
			head = -1;
			tail = 0;
			queue[0] = i;
			closure[i][i] = true;
			while (head < tail) {
				j = queue[++head];
				// search along epsilon edge
				for (k = 0; k < state; ++k)
					if ((!closure[i][k]) && (g[j][symbol][k])) {
						queue[++tail] = k;
						closure[i][k] = true;
					}
			}
		}
	}

	// parse a regular expression from position s to t, returning the
	// corresponding
	// Epsilon-NFA. The array of length 2 contains the start state at the first
	// position
	// and the final state at the second position
	static int[] parse(String re, int s, int t) {
		int[] st;
		int i;

		// single symbol
		if (s == t) {
			st = new int[2];
			st[0] = incCapacity();
			st[1] = incCapacity();
			if (re.charAt(s) == 'e') // epsilon
				addEdge(st[0], symbol, st[1]);
			else
				addEdge(st[0], re.charAt(s) - '0', st[1]);
			return st;
		}

		// (....)
		if ((re.charAt(s) == '(') && (re.charAt(t) == ')')) {
			if (next[s] == t)
				return parse(re, s + 1, t - 1);
		}

		// RE1+RE2
		i = s;
		while (i <= t) {
			i = next[i];

			if ((i <= t) && (re.charAt(i) == '+')) {
				int[] st1 = parse(re, s, i - 1);
				int[] st2 = parse(re, i + 1, t);
				st = union(st1[0], st1[1], st2[0], st2[1]);
				return st;
			}
			++i;
		}

		// RE1.RE2
		i = s;
		while (i <= t) {
			i = next[i];

			if ((i <= t) && (re.charAt(i) == '.')) {
				int[] st1 = parse(re, s, i - 1);
				int[] st2 = parse(re, i + 1, t);
				st = concat(st1[0], st1[1], st2[0], st2[1]);
				return st;
			}
			++i;
		}

		// (RE)*
		assert (re.charAt(t) == '*');
		int[] st1 = parse(re, s, t - 1);
		st = clo(st1[0], st1[1]);
		return st;
	}

	// calculate the corresponding ')' of '('
	static void calc_next(String re) {
		int i, j, k;
		next = new int[re.length()];
		for (i = 0; i < re.length(); ++i) {
			if (re.charAt(i) == '(') {
				k = 0;
				j = i;
				while (true) {
					if (re.charAt(j) == '(')
						++k;
					if (re.charAt(j) == ')')
						--k;
					if (k == 0)
						break;
					++j;
				}
				next[i] = j;
			} else
				next[i] = i;
		}
	}

	static boolean test(boolean[] cur, int finalstate, int level, int len, String RE) {
		boolean[] next = new boolean[state];
		int i, j, k, c;
		if (level >= len)
			return cur[finalstate];
		if (RE.charAt(level) > '0')
			c = 1;
		else
			c = 0;
		for (i = 0; i < state; ++i)
			if (cur[i]) {
				for (j = 0; j < state; ++j)
					if (g[i][c][j]) {
						for (k = 0; k < state; ++k)
							next[k] = (next[k] || closure[j][k]);
					}
			}

		boolean empty = true; // test if the state set is already empty
		for (i = 0; i < state; ++i)
			if (next[i])
				empty = false;
		if (empty)
			return false;
		return test(next, finalstate, level + 1, len, RE);
	}
	public static ArrayList epsnfa_to_nfa(int enfa[]) {
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
		return nfa;
	}
	
	public static void re_to_epsnfa(String re, String[] testRE) {
		for (int i = 0; i < testRE.length; i++) {
			System.out.println("Processing ...");
			calc_next(re);
			state = 0;
			int[] nfa = parse(re, 0, re.length() - 1);
			// calculate closure
			calc_closure();
	
			if (test(closure[nfa[0]], nfa[1], 0, testRE[i].length(), testRE[i])) {
				System.out.println("YES");
			}
			else {
				System.out.println("NO");
			}
		}
	}

	public static void main(String args[]) {
		int t;
		Scanner	sc = new Scanner(System.in);
		t = sc.nextInt();
		String asdf = sc.nextLine();
		for (int tt = 0; tt < t; tt++) {
			String re = sc.nextLine();
			int nt;
			nt = sc.nextInt();
			asdf = sc.nextLine();
			String[] testRE = new String[nt];
			for (int i = 0; i < nt; i++) {
				 testRE[i] = sc.nextLine();
			}
			re_to_epsnfa(re,testRE);
		}
	}
}
