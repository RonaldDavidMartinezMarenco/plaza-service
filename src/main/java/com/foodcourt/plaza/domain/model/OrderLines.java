package com.foodcourt.plaza.domain.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.foodcourt.plaza.domain.exception.DuplicateOrderLine;
import com.foodcourt.plaza.domain.exception.EmptyOrder;

/** Turns the request lines into order lines. Rejects an empty order and a repeated dish. */
public final class OrderLines {

    private OrderLines() {
    }

    public static List<OrderLine> require(List<PlaceOrderDraft.OrderLineDraft> drafts) {
        requirePresent(drafts);
        return distinct(drafts);
    }

    private static void requirePresent(List<PlaceOrderDraft.OrderLineDraft> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            throw new EmptyOrder();
        }
    }

    private static List<OrderLine> distinct(List<PlaceOrderDraft.OrderLineDraft> drafts) {
        Set<Long> seen = new HashSet<>();
        List<OrderLine> accepted = new ArrayList<>();
        for (PlaceOrderDraft.OrderLineDraft draft : drafts) {
            accepted.add(line(draft, seen));
        }
        return List.copyOf(accepted);
    }

    private static OrderLine line(PlaceOrderDraft.OrderLineDraft draft, Set<Long> seen) {
        requireDraft(draft);
        int quantity = new OrderQuantity(draft.quantity()).value();
        requireNewDish(draft.dishId(), seen);
        return new OrderLine(draft.dishId(), quantity);
    }

    private static void requireDraft(PlaceOrderDraft.OrderLineDraft draft) {
        if (draft == null || draft.dishId() == null) {
            throw new EmptyOrder();
        }
    }

    private static void requireNewDish(Long dishId, Set<Long> seen) {
        if (!seen.add(dishId)) {
            throw new DuplicateOrderLine();
        }
    }
}
