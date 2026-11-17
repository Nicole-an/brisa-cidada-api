package br.com.brisacidada.api.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " " + id + " não encontrado(a).");
    }
}
