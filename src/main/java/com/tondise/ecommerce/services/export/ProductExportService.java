package com.tondise.ecommerce.services.export;

import com.tondise.ecommerce.dao.models.Product;
import com.tondise.ecommerce.dao.repository.ProductRepository;
import com.tondise.ecommerce.dao.utils.ExcelReportGenerator;
import com.tondise.ecommerce.dao.utils.PdfReportGenerator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductExportService {

    private static final List<String> HEADERS = List.of(
            "Nom", "Slug", "Catégorie", "Prix de base", "Stock", "Vedette");

    private final ProductRepository productRepository;
    private final PdfReportGenerator pdfReportGenerator;
    private final ExcelReportGenerator excelReportGenerator;

    public byte[] exportPdf() {
        return pdfReportGenerator.generate("Catalogue produits Tondise", HEADERS, rows());
    }

    public byte[] exportExcel() {
        return excelReportGenerator.generate("Produits", HEADERS, rows());
    }

    private List<List<String>> rows() {
        return productRepository.findAll().stream().map(this::toRow).toList();
    }

    private List<String> toRow(Product product) {
        return List.of(
                product.getName(),
                product.getSlug(),
                product.getCategory() != null ? product.getCategory().getName() : "-",
                product.getBasePrice().toPlainString(),
                String.valueOf(product.getStockQuantity()),
                product.isFeatured() ? "Oui" : "Non");
    }
}
