package dsa.model.data;

import java.io.IOException;
import java.util.StringTokenizer;

public class Cloth {
	
	public Cloth(String name, int ks, int be) {
		this.name = name;
		this.ks = ks;
		this.be = be;
	}
	
	private String name;
	private int ks;
	private int be;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getKS() {
		return ks;
	}
	public void setKS(int ks) {
		this.ks = ks;
	}
	public int getBE() {
		return be;
	}
	public void setBE(int be) {
		this.be = be;
	}
	
	
	public static Cloth read(String line, int lineNr) throws IOException {
		StringTokenizer tokenizer = new StringTokenizer(line, ";");
		if (tokenizer.countTokens() < 3) {
			throw new IOException("Zeile " + lineNr + ": Falsches Format für Kleidung!");
		}
		String name = tokenizer.nextToken();
		try {
			int ks = Integer.parseInt(tokenizer.nextToken());
			int be = Integer.parseInt(tokenizer.nextToken());
			if (ks < 0) {
				throw new IOException("Zeile " + lineNr + ": Negativer KS bei Kleidung!");
			}
			if (be < 0) {
				throw new IOException("Zeile " + lineNr + ": Negative BE bei Kleidung!");
			}
			return new Cloth(name, ks, be);
		}
		catch (NumberFormatException e) {
			throw new IOException("Zeile " + lineNr + ": Falsches Format für Kleidung!", e);
		}
	}
	
	public String write() {
		return name + ";" + ks + ";" + be;
	}

}
