

module user {
    'use strict';
    export class LoginController {
        constructor($scope: user.ILoginScope, $state, $location, factory: backend.Factory,
            Page: common.PageUtils, BodyClass, UrlStore: utils.DataStore) {

            BodyClass.setClass('login');
            Page.setTitle('Login | 123Click');
            $scope.error_message = '';
            var sessionid: string = common.Utils.getCookie(common.Config.COOKIE_NAME);
            if (sessionid.length > 0) {
                factory.sessionService.getCurrentUser(sessionid, function (response) {
                    if (response.code === 1) {
                        factory.userInfo.setUserInfo(response.data);
                        $state.transitionTo('main.website.list', { page: 1 });  // TODO : routing base role
                    }
                });
            }

            
            $scope.login = (uname: string, pass: string) => {
                if (uname.length === 0 || pass.length === 0)
                    return;

                factory.sessionService.login(uname, pass, function (response) {
                    if (response.code === 1) {
                        var sessionid: string = response.msg;
                        if (sessionid.length > 0) {
                            common.Utils.setCookieHour(common.Config.COOKIE_NAME, sessionid, 6);

                            factory.sessionService.getCurrentUser(sessionid, function (response) {
                                if (response.code === 1) {
                                    factory.userInfo.setUserInfo(response.data);

                                    var storeURL = UrlStore.get("cburl")|| {};
                                    var callbackUrl: string = storeURL["url"];

                                    if (callbackUrl) {
                                        UrlStore.remove("cburl");
                                        $location.path(callbackUrl);
                                        Page.setTitle(storeURL["title"]);
                                        return;
                                    }
                                    $state.transitionTo('main.website.list', { page: 1 });  // TODO : routing base role
                                }
                            });
                            return;
                        }
                    }
                    // login fail
                    if (response.status === 500) {
                        $scope.error_message = response.msg;
                        return;
                    }
                    $scope.error_message = response.msg;
                });
            };

            $scope.goSignUp = () => {
                $state.transitionTo('signup');
            };
        }
    }

    export class SignUpController {
        constructor($scope: user.ISignUpScope, $state, Page: common.PageUtils, BodyClass, factory: backend.Factory) {
            BodyClass.setClass('login');
            Page.setTitle('Sign Up | 123Click');
            $scope.error_message = '';
            $scope.agree = true;
            $scope.username = ''; $scope.password = ''; $scope.confirmPassword = '';
            $scope.signup = () => {
                if ($scope.formsignup.$valid && $scope.password === $scope.confirmPassword && $scope.agree)
                    factory.userService.signup($scope.username, $scope.email, $scope.password, function (response) {

                        if (response && response.id > 0)
                            $scope.error_message = "Registered successfull!!";
                        else
                            $scope.error_message = response;
                    });
            };

            $scope.checkPassword = () => {
                $scope.error_message = ($scope.password !== $scope.confirmPassword) ? "Passwords don't match!" : "";
            };

            $scope.goLogin = () => {
                $state.transitionTo('login');
            };

            $scope.isInvalid = (): boolean => {
                if ($scope.formsignup.$valid && $scope.password === $scope.confirmPassword && $scope.agree)
                //if ($scope.username.length > 0 && $scope.password.length > 0 && $scope.password === $scope.confirmPassword && $scope.agree)
                    return false;
                return true;
            };

            $scope.getSignUpStatus = (): string => {
                if ($scope.error_message === "Registered successfull!!")
                    return 'alert-success';
                return 'alert-error';
            };
        }
    }
}