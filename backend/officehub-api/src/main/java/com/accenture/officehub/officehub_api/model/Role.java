package com.accenture.officehub.officehub_api.model;

public enum Role {
	ADMIN,
	GESTOR,
	FUNCIONARIO;

	/** Valores aceitos pelo frontend (`SessionUser.role`). */
	public String toApiRole() {
		return switch (this) {
			case ADMIN -> "admin";
			case GESTOR -> "manager";
			case FUNCIONARIO -> "employee";
		};
	}
}
