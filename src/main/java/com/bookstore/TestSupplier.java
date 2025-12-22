package com.bookstore;

import com.bookstore.dao.SupplierDao;
import com.bookstore.dao.SupplyDao;
import com.bookstore.model.Supplier;
import com.bookstore.model.Supply;

import java.sql.SQLException;
import java.util.List;

/**
 * 测试供应商与供货关系：
 *  1）查询并打印所有供应商；
 *  2）对每个供应商，查询其可供应的书目；
 *  3）示例：查询某一本书（例如 B001）的所有供应商。
 */
public class TestSupplier {

    public static void main(String[] args) {
        SupplierDao supplierDao = new SupplierDao();
        SupplyDao supplyDao = new SupplyDao();

        try {
            System.out.println("当前所有供应商：");
            List<Supplier> suppliers = supplierDao.findAll();
            for (Supplier s : suppliers) {
                System.out.println(s);
                List<Supply> supplies = supplyDao.findBySupplierId(s.getSupplierId());
                System.out.println("  可供应的书目：");
                for (Supply sp : supplies) {
                    System.out.println("    " + sp);
                }
                System.out.println();
            }

            System.out.println("查询书号 B001 的所有供应商：");
            List<Supply> b1Supplies = supplyDao.findByBookId("B001");
            for (Supply sp : b1Supplies) {
                System.out.println(sp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


