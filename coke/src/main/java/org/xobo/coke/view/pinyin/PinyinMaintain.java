package org.xobo.coke.view.pinyin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;
import org.xobo.coke.dao.CokeHibernate;
import org.xobo.coke.entity.PinyinConverter;
import org.xobo.coke.model.Pinyin;
import org.xobo.coke.querysupporter.service.impl.SynonymServiceImpl;
import org.xobo.coke.utility.PinyinUtility;

import com.bstek.dorado.annotation.DataProvider;
import com.bstek.dorado.annotation.Expose;

@Service("coke.pinyinMaintain")
public class PinyinMaintain {

  @DataProvider
  public Collection<PinyinConverter> loadPinyinConverters() {
    Collection<PinyinConverter> pinyinEntityList = new ArrayList<PinyinConverter>();
    Collection<Entry<Class<?>, Map<String, Collection<String>>>> clazzList =
        SynonymServiceImpl.getPinyinmap().getData().entrySet();
    for (Entry<Class<?>, Map<String, Collection<String>>> clazz : clazzList) {
      PinyinConverter pinyinConverter = new PinyinConverter();
      pinyinConverter.setClazz(clazz.getKey().getName());

      Map<String, Collection<String>> pm = clazz.getValue();
      Set<Entry<String, Collection<String>>> entries = pm.entrySet();
      if (!entries.isEmpty()) {
        Entry<String, Collection<String>> entry = entries.iterator().next();
        pinyinConverter.setProperty(entry.getKey());

        Collection<String> p = entry.getValue();
        for (String string : p) {
          if (string.toLowerCase().contains("quan")) {
            pinyinConverter.setQuanpinProperty(string);
          } else if (string.toLowerCase().contains("jian")) {
            pinyinConverter.setJianpinProperty(string);
          }
        }
        pinyinEntityList.add(pinyinConverter);
      }

    }
    return pinyinEntityList;
  }

  @Expose
  public void batchConvert(Map<String, Object> parameter) {
    String clazzName = (String) parameter.get("clazz");
    String property = (String) parameter.get("property");
    String quanProperty = (String) parameter.get("quanpinProperty");
    String jianProperty = (String) parameter.get("jianpinProperty");
    Integer batchSize = (Integer) parameter.get("batchSize");

    if (StringUtils.isEmpty(property)) {
      property = "name";
    }

    if (batchSize == null) {
      batchSize = 200;
    }
    Session session = cokeHibernate.getSession();

    Collection<?> list;
    do {
      DetachedCriteria dc = createDetachedCriteria(clazzName);
      dc.add(Restrictions.or(Restrictions.isNull(quanProperty), Restrictions.isNull(jianProperty)));
      dc.add(Restrictions.isNotNull(property));
      Criteria criteria = dc.getExecutableCriteria(session);
      criteria.setFirstResult(0);
      criteria.setMaxResults(batchSize);
      list = criteria.list();

      Transaction transaction = session.beginTransaction();
      for (Object object : list) {
        try {
          String value = BeanUtils.getProperty(object, property);
          Collection<Pinyin> pinyins = PinyinUtility.toPinyin(value);
          String quanValue;
          String jianValue;
          if (!pinyins.isEmpty()) {
            Pinyin pinyin = pinyins.iterator().next();
            quanValue = pinyin.getQuan();
            jianValue = pinyin.getJian();
          } else {
            quanValue = jianValue = value;
          }
          BeanUtils.setProperty(object, quanProperty, quanValue);
          BeanUtils.setProperty(object, jianProperty, jianValue);
          session.update(object);
        } catch (Exception e) {
          e.printStackTrace();
          break;
        }
      }
      transaction.commit();
      session.flush();
      session.clear();
    } while (!list.isEmpty());

  }

  @Expose
  public void prewPinyin(Map<String, Object> parameter) throws IllegalAccessException,
      InvocationTargetException, NoSuchMethodException {
    String clazzName = (String) parameter.get("clazz");
    String property = (String) parameter.get("property");
    String quanProperty = (String) parameter.get("quanpinProperty");
    String jianProperty = (String) parameter.get("jianpinProperty");
    Integer batchSize = (Integer) parameter.get("batchSize");

    if (StringUtils.isEmpty(property)) {
      property = "name";
    }

    if (batchSize == null) {
      batchSize = 200;
    }
    Session session = cokeHibernate.getSession();

    Collection<?> list;
    Map<String, Integer> wordMap = new HashMap<String, Integer>();
    do {
      DetachedCriteria dc = createDetachedCriteria(clazzName);
      dc.add(Restrictions.or(Restrictions.isNull(quanProperty), Restrictions.isNull(jianProperty)));
      dc.add(Restrictions.isNotNull(property));
      Criteria criteria = dc.getExecutableCriteria(session);
      criteria.setFirstResult(0);
      criteria.setMaxResults(batchSize);
      list = criteria.list();

      for (Object object : list) {
        String value = BeanUtils.getProperty(object, property);
        if (value != null && value.length() > 1) {
          addSentence(wordMap, value);
        }
      }
      session.clear();
    } while (!list.isEmpty());

  }

  public static void addSentence(Map<String, Integer> wordMap, String sentence) {
    if (sentence != null && sentence.length() > 1) {
      for (int i = 0; i <= sentence.length(); i++) {
        for (int j = i + 1; j <= sentence.length(); j++) {
          addWord(wordMap, sentence.substring(i, j));
        }
      }
    }
  }

  public static void addWord(Map<String, Integer> wordMap, String word) {
    Integer count = wordMap.get(word);
    if (count == null) {
      count = 0;
    }
    count++;
    wordMap.put(word, count);
  }

  public static void main(String[] args) {
    Map<String, Integer> wordMap = new LinkedHashMap<String, Integer>();
    addSentence(wordMap, "大家好我是小不哥哥");
    System.out.println(wordMap);
  }

  public DetachedCriteria createDetachedCriteria(String name) {
    DetachedCriteria dc = null;
    if (name.contains(".")) {
      Class<?> clazz;
      try {
        clazz = Class.forName(name);
        dc = DetachedCriteria.forClass(clazz);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    } else {
      dc = DetachedCriteria.forEntityName(name);
    }
    return dc;
  }

  @Resource(name = CokeHibernate.BEAN_ID)
  private CokeHibernate cokeHibernate;
}
