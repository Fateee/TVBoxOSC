package com.common.network.model;

public class PagingResult {
    public boolean isFirstPage;
    public boolean isEmpty;
    public boolean hasNextPage;

    public PagingResult(boolean isFirstPage, boolean isEmpty, boolean hasNextPage) {
        this.isFirstPage = isFirstPage;
        this.isEmpty = isEmpty;
        this.hasNextPage = hasNextPage;
    }
}
