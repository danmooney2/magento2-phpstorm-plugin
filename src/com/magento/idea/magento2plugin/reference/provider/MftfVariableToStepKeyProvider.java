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
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.magento.idea.magento2plugin.reference.xml.PolyVariantReferenceBase;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.SelectorIndex;
import com.magento.idea.magento2plugin.stubs.indexes.mftf.StepKeyIndex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MftfVariableToStepKeyProvider extends PsiReferenceProvider {

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        List<PsiReference> psiReferences = new ArrayList<>();

        String origValue = StringUtil.unquoteString(element.getText());

        String modifiedValue = origValue.replaceAll("\\$", "").toString();

        XmlFile xmlFile = (XmlFile) element.getContainingFile();
        XmlTag xmlRootTag = xmlFile.getRootTag();

        for (XmlTag childTag : xmlRootTag.getSubTags()) {
            if (childTag.getAttributeValue("name") == null ||
                childTag.getAttributeValue("name").isEmpty()
            ) {
                continue;
            }

            addAttributeValue(
                childTag,
                element,
                modifiedValue,
                psiReferences
            );
        }

        return psiReferences.toArray(new PsiReference[psiReferences.size()]);
    }

    private void addAttributeValue(XmlTag parentTag, PsiElement element, String valueToMatch, List<PsiReference> psiReferences) {
        for (XmlTag childTag: parentTag.getSubTags()) {
            List<PsiElement> psiElements = new ArrayList<>();
            XmlAttribute stepKeyAttribute = childTag.getAttribute("stepKey");

            if (stepKeyAttribute != null &&
                stepKeyAttribute.getValueElement() != null &&
                stepKeyAttribute.getValueElement().getValue().equals(valueToMatch)
            ) {
                psiElements.add(stepKeyAttribute.getValueElement());
                psiReferences.add(new PolyVariantReferenceBase(element, psiElements));
            }

            addAttributeValue(childTag, element, valueToMatch, psiReferences);
        }
    }
}
