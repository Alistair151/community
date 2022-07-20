package com.alistair.community.dao;

import org.springframework.stereotype.Repository;

@Repository("H")
public class AlphaDaoHibernateImpel implements AlphaDao{
    @Override
    public String select() {
        return "Hibernate";
    }
}
