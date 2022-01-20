package com.podo.modules.account.form;

import lombok.Data;

@Data
public class Notifications {

    private boolean clubCreatedByEmail;

    private boolean clubCreatedByWeb;

    private boolean clubEnrollmentByEmail;

    private boolean clubEnrollmentByWeb;

    private boolean clubUpdatedByEmail;

    private boolean clubUpdatedByWeb;

}
