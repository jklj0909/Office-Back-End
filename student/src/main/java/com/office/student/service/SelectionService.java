package com.office.student.service;

import com.office.student.repository.SelectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SelectionService {
    private SelectionMapper selectionMapper;

    @Autowired
    public void setSelectionMapper(SelectionMapper selectionMapper) {
        this.selectionMapper = selectionMapper;
    }
}
