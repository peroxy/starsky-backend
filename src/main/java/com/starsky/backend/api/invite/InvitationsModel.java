package com.starsky.backend.api.invite;

import io.swagger.v3.oas.annotations.media.Schema;

/*
* InvitationsModel used by starsky-mail API
* */

public class InvitationsModel {
    private final String managerName;
    private final String employeeName;
    private final String employeeEmail;
    private final String registerUrl;

    public String getManagerName() {
        return managerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public String getRegisterUrl() {
        return registerUrl;
    }

    public InvitationsModel(String managerName, String employeeName, String employeeEmail, String registerUrl) {
        this.managerName = managerName;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.registerUrl = registerUrl;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InvitationsModel {\n");

        sb.append("    managerName: ").append(toIndentedString(managerName)).append("\n");
        sb.append("    employeeName: ").append(toIndentedString(employeeName)).append("\n");
        sb.append("    employeeEmail: ").append(toIndentedString(employeeEmail)).append("\n");
        sb.append("    registerUrl: ").append(toIndentedString(registerUrl)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
