package sk.seges.sesam.core.pap.test.model.utils;

import sk.seges.sesam.core.pap.test.TestAnnotationMirror;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by fat on 10/20/2014.
 */
public abstract class TestAnnotatedConstruct implements AnnotatedConstruct {

    private List<AnnotationMirror> annotationMirrors;

    protected abstract Annotation[] getAnnotations();

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        if (annotationMirrors == null) {
            annotationMirrors = new LinkedList<AnnotationMirror>();

            for (Annotation annotation: getAnnotations()) {
                TestAnnotationMirror testAnnotationMirror = new TestAnnotationMirror(new TestDeclaredType(new TestTypeElement(annotation.annotationType())), annotation);
                annotationMirrors.add(testAnnotationMirror);
            }
        }
        return annotationMirrors;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        Annotation[] annotations = getAnnotations();
        if (annotations == null) {
            return null;
        }

        for (Annotation annotation: annotations) {
            if (annotation.annotationType().getName().equals(annotationType.getName())) {
                return (A) annotation;
            }
        }

        return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        Annotation[] annotations = getAnnotations();
        if (annotations == null) {
            return null;
        }

        List<A> result = new ArrayList<A>();

        for (Annotation annotation: annotations) {
            if (annotation.annotationType().getName().equals(annotationType.getName())) {
                result.add((A) annotation);
            }
        }

        return (A[]) result.toArray();
    }
}
