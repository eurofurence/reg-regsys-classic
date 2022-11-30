package org.eurofurence.regsys.repositories.lowlevel;

public class ResponseWithDto<T> {
    public T dto;
    public int status;
    public String location;
}
