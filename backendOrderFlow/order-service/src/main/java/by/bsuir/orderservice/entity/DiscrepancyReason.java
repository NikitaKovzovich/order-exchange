package by.bsuir.orderservice.entity;




public enum DiscrepancyReason {
	DAMAGE("Бой"),
	SHORTAGE("Недостача"),
	WRONG_ITEM("Пересорт"),
	EXCESS("Излишек"),
	OTHER("Прочее");

	private final String displayName;

	DiscrepancyReason(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
}
