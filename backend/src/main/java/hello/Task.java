package hello;

import org.springframework.data.annotation.Id;

public class Task {

	@Id private String id;

	private String description;
	private Boolean checked;

	public Boolean getChecked() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
