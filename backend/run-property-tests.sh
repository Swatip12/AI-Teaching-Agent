#!/bin/bash

# Script to run property-based tests for AI Teaching Platform
# Requires Maven to be installed

echo "Running AI Teaching Platform Property-Based Tests..."
echo "=================================================="

# Test 1: AI Adaptive Response Property
echo "Running AI Adaptive Response Property Test..."
mvn test -Dtest=AIAdaptiveResponseProperty -q

# Test 2: Error Specific Guidance Property
echo "Running Error Specific Guidance Property Test..."
mvn test -Dtest=ErrorSpecificGuidanceProperty -q

# Test 3: Success Reinforcement Property
echo "Running Success Reinforcement Property Test..."
mvn test -Dtest=SuccessReinforcementProperty -q

# Test 4: Progress Based Unlocking Property
echo "Running Progress Based Unlocking Property Test..."
mvn test -Dtest=ProgressBasedUnlockingProperty -q

# Test 5: Subject Isolation Property
echo "Running Subject Isolation Property Test..."
mvn test -Dtest=SubjectIsolationProperty -q

echo "=================================================="
echo "Property-based tests completed!"
echo ""
echo "Note: These tests validate the following properties:"
echo "- Property 3: AI adaptive response behavior (Requirements 1.4, 1.5)"
echo "- Property 6: Error-specific guidance (Requirements 3.2, 3.4)"
echo "- Property 7: Success reinforcement (Requirements 3.5)"
echo "- Property 9: Progress-based unlocking (Requirements 4.4, 4.5)"
echo "- Property 10: Subject isolation and paths (Requirements 5.2, 5.3)"