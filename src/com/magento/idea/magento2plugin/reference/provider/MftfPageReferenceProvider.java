package com.magento.idea.magento2plugin.reference.provider;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.ActionGroupIndex;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.PageIndex;
import com.magento.idea.magento2plugin.xml.XmlPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import sun.jvm.hotspot.debugger.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MftfPageReferenceProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        List<PsiReference> psiReferences = new ArrayList<>();

        String origValue = StringUtil.unquoteString(element.getText());
        Logger.getInstance("pizzatime").info("Looking up in ActionGroupIndex: " + origValue);

        Collection<VirtualFile> containingFiles = FileBasedIndex.getInstance()
            .getContainingFiles(
                PageIndex.KEY,
                origValue,
                GlobalSearchScope.getScopeRestrictedByFileTypes(
                    GlobalSearchScope.allScope(element.getProject()),
                    XmlFileType.INSTANCE
                )
            );

        PsiManager psiManager = PsiManager.getInstance(element.getProject());

        List<PsiElement> psiElements = new ArrayList<>();

        for (VirtualFile virtualFile: containingFiles) {
            XmlFile xmlFile = (XmlFile) psiManager.findFile(virtualFile);

            if (xmlFile == null) {
                continue;
            }

            Collection<XmlAttributeValue> valueElements = XmlPsiTreeUtil
                    .findAttributeValueElements(xmlFile, "page", "name", origValue);


            Logger.getInstance("pizzatime").info("valueElements length: " + valueElements.size());

            psiElements.addAll(valueElements);
        }

        if (psiElements.size() > 0) {
            psiReferences.add(new PolyVariantReferenceBase(element, psiElements));
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }
}
