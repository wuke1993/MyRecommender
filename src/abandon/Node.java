package abandon;
/**
 * @author: wuke 
 * @date  : 20170527 10:55:58
 * Title  : Node
 * Description : 
 */
public class Node {
	
	private String name;
	private int value;
	private int category;
	private String symbol;
	
	public Node() {
		
	}
	
	public Node(String name, int value, int category, String symbol) {
		this.name = name;
		this.value = value;
		this.category = category;
		this.symbol = symbol;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
}
