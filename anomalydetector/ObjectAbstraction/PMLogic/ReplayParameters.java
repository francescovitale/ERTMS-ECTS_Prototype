package ObjectAbstraction.PMLogic;

public class ReplayParameters {
	int p,c,m,r;
	boolean end;
	
	public ReplayParameters() {
		p = 0;
		c = 0;
		m = 0;
		r = 0;
		end = false;
	}
	public ReplayParameters(int p_in, int c_in, int m_in, int r_in, boolean e) {
		p = p_in;
		c = c_in;
		m = m_in;
		r = r_in;
		end = e;
	}
	public int getP() {
		return p;
	}
	public void setP(int p) {
		this.p = p;
	}
	public int getC() {
		return c;
	}
	public void setC(int c) {
		this.c = c;
	}
	public int getM() {
		return m;
	}
	public void setM(int m) {
		this.m = m;
	}
	public int getR() {
		return r;
	}
	public void setR(int r) {
		this.r = r;
	}
	public boolean isEnd() {
		return end;
	}
	public void setEnd(boolean end) {
		this.end = end;
	};
}
