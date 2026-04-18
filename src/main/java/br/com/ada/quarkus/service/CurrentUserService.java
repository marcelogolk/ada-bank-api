package br.com.ada.quarkus.service;

import br.com.ada.quarkus.model.LoggedUser;

public interface CurrentUserService {

    LoggedUser getLoggedUser();
}