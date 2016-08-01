package org.xobo.coke.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;
import org.xobo.coke.dao.CokeHibernate;
import org.xobo.coke.model.DictEntry;
import org.xobo.coke.service.DictEntriesProvider;
import org.xobo.coke.service.RbacInfoService;

import com.bstek.bdf2.core.business.IDept;

@Repository
public class RbacInfoServiceImpl implements RbacInfoService,DictEntriesProvider {
	@Resource
	private CokeHibernate cokeHibernate;
	@Override
	public Collection<DictEntry> lookup(Object categorykey, Object... otherParameters) {
		if(categorykey==null||categorykey.equals("")){
			categorykey="DefaultDept";
		}
		String sql = "FROM "+categorykey+" D ";
		@SuppressWarnings("unchecked")
		List<IDept> depts = cokeHibernate.getSessionFactory().openSession().createQuery(sql).list();
		List<DictEntry> entrys = new ArrayList<DictEntry>();
		for(IDept d :depts){
			DictEntry entry = new DictEntry();
			entry.setKey(d.getId());
			entry.setValue(d.getName());
			entrys.add(entry);
		}
		return entrys;
	}
	@Override
	public String getType() {
		return "coke.depts";
	}

}
