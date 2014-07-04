/// <reference path="../../common/scopes.ts"/>
/// <reference path="../user/models.ts"/>

'use strict';
module user {
    export interface ILoginScope extends ng.IScope {
        error_message: string;
        login(uname: string, pass: string);
        logout(sessionid: string);
        goSignUp();
    }

    export interface ISignUpScope extends ng.IScope {
        agree: boolean;
        username: string;
        email: string;
        password: string;
        confirmPassword: string;
        error_message: string;
        formsignup: any;
        signup(uname: string, pass: string);
        getSignUpStatus(): string;
        checkPassword();
        goLogin();
        isInvalid(): boolean;
    }
}